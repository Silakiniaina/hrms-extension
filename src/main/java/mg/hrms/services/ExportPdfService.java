package mg.hrms.services;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import mg.hrms.models.Employee;
import mg.hrms.models.SalaryComponent;
import mg.hrms.models.SalarySlip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class ExportPdfService {

    private static final Logger logger = LoggerFactory.getLogger(ExportPdfService.class);
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);

    /* -------------------------------------------------------------------------- */
    /*                    Processing the PaySlipPdf generation                    */
    /* -------------------------------------------------------------------------- */
    public void generatePayslipPdf(Employee employee, SalarySlip payslip, OutputStream outputStream) throws Exception {
        logger.info("Generating PDF for payslip: {} for employee: {}", payslip.getSlipId(), employee.getEmployeeId());
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("PAYSLIP", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font companyFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Paragraph companyInfo = new Paragraph(employee.getCompany() != null ? employee.getCompany().getName() : "N/A", companyFont);
            companyInfo.setAlignment(Element.ALIGN_CENTER);
            document.add(companyInfo);

            document.add(new com.lowagie.text.Chunk("\n"));

            Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Paragraph employeeInfo = new Paragraph();
            employeeInfo.add(new com.lowagie.text.Chunk("Employee: ", infoFont));
            employeeInfo.add(new com.lowagie.text.Chunk(employee.getFirstName() + " " + employee.getLastName(), infoFont));
            employeeInfo.add(new com.lowagie.text.Chunk("\n"));
            employeeInfo.add(new com.lowagie.text.Chunk("Employee ID: ", infoFont));
            employeeInfo.add(new com.lowagie.text.Chunk(employee.getEmployeeId(), infoFont));
            employeeInfo.add(new com.lowagie.text.Chunk("\n"));
            employeeInfo.add(new com.lowagie.text.Chunk("Period: ", infoFont));
            employeeInfo.add(new com.lowagie.text.Chunk(payslip.getPostingDate() != null ? payslip.getPostingDate().toString() : "N/A", infoFont));
            document.add(employeeInfo);

            document.add(new com.lowagie.text.Chunk("\n"));
            document.add(createEarningsTable(payslip));
            document.add(new com.lowagie.text.Chunk("\n"));
            document.add(createDeductionsTable(payslip));
            document.add(new com.lowagie.text.Chunk("\n"));
            document.add(createNetPaySection(payslip));
        } catch (Exception e) {
            logger.error("Failed to generate PDF for payslip: {} - Error: {}", payslip.getSlipId(), e.getMessage());
            throw new Exception("PDF generation failed: " + e.getMessage(), e);
        } finally {
            if (document.isOpen()) document.close();
        }
        logger.info("PDF generated successfully for payslip: {}", payslip.getSlipId());
    }

    /* -------------------------------------------------------------------------- */
    /*                        Create the table for earnings                       */
    /* -------------------------------------------------------------------------- */
    private PdfPTable createEarningsTable(SalarySlip payslip) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell(new Phrase("Earnings"));
        cell.setColspan(2);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new Color(220, 220, 220));
        table.addCell(cell);

        table.addCell(new Phrase("Description"));
        table.addCell(new Phrase("Amount"));

        if (payslip.getEarnings() != null) {
            for (SalaryComponent earning : payslip.getEarnings()) {
                table.addCell(earning.getName() != null ? earning.getName() : "N/A");
                table.addCell(currencyFormat.format(earning.getAmount()));
            }
        }

        PdfPCell totalCell = new PdfPCell(new Phrase("Total Earnings"));
        totalCell.setBackgroundColor(new Color(220, 220, 220));
        table.addCell(totalCell);
        table.addCell(new Phrase(currencyFormat.format(payslip.getGrossPay() != null ? payslip.getGrossPay() : 0.0)));
        return table;
    }

    /* -------------------------------------------------------------------------- */
    /*                       Create the table for deductions                      */
    /* -------------------------------------------------------------------------- */
    private PdfPTable createDeductionsTable(SalarySlip payslip) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell(new Phrase("Deductions"));
        cell.setColspan(2);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new Color(220, 220, 220));
        table.addCell(cell);

        table.addCell(new Phrase("Description"));
        table.addCell(new Phrase("Amount"));

        if (payslip.getDeductions() != null) {
            for (SalaryComponent deduction : payslip.getDeductions()) {
                table.addCell(deduction.getName() != null ? deduction.getName() : "N/A");
                table.addCell(currencyFormat.format(deduction.getAmount()));
            }
        }

        PdfPCell totalCell = new PdfPCell(new Phrase("Total Deductions"));
        totalCell.setBackgroundColor(new Color(220, 220, 220));
        table.addCell(totalCell);
        table.addCell(new Phrase(currencyFormat.format(
                payslip.getGrossPay() != null && payslip.getNetPay() != null
                        ? payslip.getGrossPay() - payslip.getNetPay() : 0.0)));
        return table;
    }

    /* -------------------------------------------------------------------------- */
    /*                     Create table for the netPay section                    */
    /* -------------------------------------------------------------------------- */
    private PdfPTable createNetPaySection(SalarySlip payslip) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);

        PdfPCell netPayCell = new PdfPCell(new Phrase("Net Pay"));
        netPayCell.setBackgroundColor(new Color(200, 230, 200));
        table.addCell(netPayCell);
        table.addCell(new Phrase(currencyFormat.format(payslip.getNetPay() != null ? payslip.getNetPay() : 0.0)));
        return table;
    }
}
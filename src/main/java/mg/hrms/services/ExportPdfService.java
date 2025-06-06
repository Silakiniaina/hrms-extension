package mg.hrms.services;

import mg.hrms.models.Employee;
import mg.hrms.models.SalarySlip;
import org.springframework.stereotype.Service;

// Add these imports for iText PDF generation
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

@Service
public class ExportPdfService {

    public void generatePayslipPdf(Employee employee, SalarySlip payslip, OutputStream outputStream) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        // Add title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("PAYSLIP", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // Add company info
        Font companyFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Paragraph companyInfo = new Paragraph(employee.getCompany(), companyFont);
        companyInfo.setAlignment(Element.ALIGN_CENTER);
        document.add(companyInfo);

        // Add spacing
        document.add(Chunk.NEWLINE);

        // Add employee info
        Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Paragraph employeeInfo = new Paragraph();
        employeeInfo.add(new Chunk("Employee: ", infoFont));
        employeeInfo.add(new Chunk(employee.getFirstName() + " " + employee.getLastName(), infoFont));
        employeeInfo.add(Chunk.NEWLINE);
        employeeInfo.add(new Chunk("Employee ID: ", infoFont));
        employeeInfo.add(new Chunk(employee.getEmployeeId(), infoFont));
        employeeInfo.add(Chunk.NEWLINE);
        employeeInfo.add(new Chunk("Period: ", infoFont));
        employeeInfo.add(new Chunk(payslip.getPostingDate().toString(), infoFont));
        document.add(employeeInfo);

        // Add spacing
        document.add(Chunk.NEWLINE);

        // Add earnings table
        document.add(createEarningsTable(payslip));
        document.add(Chunk.NEWLINE);

        // Add deductions table
        document.add(createDeductionsTable(payslip));
        document.add(Chunk.NEWLINE);

        // Add net pay
        document.add(createNetPaySection(payslip));

        document.close();
    }

    private PdfPTable createEarningsTable(SalarySlip payslip) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        // Table header
        PdfPCell cell = new PdfPCell(new Phrase("Earnings"));
        cell.setColspan(2);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new Color(220, 220, 220));
        table.addCell(cell);

        // Table subheaders
        table.addCell(new Phrase("Description"));
        table.addCell(new Phrase("Amount"));

        // Earnings rows
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        for (Map<String, Object> earning : payslip.getEarnings()) {
            table.addCell(earning.get("salary_component").toString());
            table.addCell(currencyFormat.format(earning.get("amount")));
        }

        // Total earnings
        PdfPCell totalCell = new PdfPCell(new Phrase("Total Earnings"));
        totalCell.setColspan(1);
        totalCell.setBackgroundColor(new Color(220, 220, 220));
        table.addCell(totalCell);
        table.addCell(new Phrase(currencyFormat.format(payslip.getGrossPay())));

        return table;
    }

    private PdfPTable createDeductionsTable(SalarySlip payslip) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        // Table header
        PdfPCell cell = new PdfPCell(new Phrase("Deductions"));
        cell.setColspan(2);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new Color(220, 220, 220));
        table.addCell(cell);

        // Table subheaders
        table.addCell(new Phrase("Description"));
        table.addCell(new Phrase("Amount"));

        // Deductions rows
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        for (Map<String, Object> deduction : payslip.getDeductions()) {
            table.addCell(deduction.get("salary_component").toString());
            table.addCell(currencyFormat.format(deduction.get("amount")));
        }

        // Total deductions
        PdfPCell totalCell = new PdfPCell(new Phrase("Total Deductions"));
        totalCell.setColspan(1);
        totalCell.setBackgroundColor(new Color(220, 220, 220));
        table.addCell(totalCell);
        table.addCell(new Phrase(currencyFormat.format(payslip.getGrossPay() - payslip.getNetPay())));

        return table;
    }

    private PdfPTable createNetPaySection(SalarySlip payslip) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        PdfPCell netPayCell = new PdfPCell(new Phrase("Net Pay"));
        netPayCell.setBackgroundColor(new Color(200, 230, 200));
        table.addCell(netPayCell);
        table.addCell(new Phrase(currencyFormat.format(payslip.getNetPay())));

        return table;
    }
}

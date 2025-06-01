package mg.hrms.models;

public class Breadcrumb {
    private String name;
    private String url;
    
    public Breadcrumb() {}
    
    public Breadcrumb(String name) {
        this.name = name;
        this.url = null; 
    }
    
    public Breadcrumb(String name, String url) {
        this.name = name;
        this.url = url;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    // Static helper methods for common breadcrumb patterns
    public static java.util.List<Breadcrumb> create(String... items) {
        java.util.List<Breadcrumb> breadcrumbs = new java.util.ArrayList<>();
        
        // Add Home as first item (always linked)
        breadcrumbs.add(new Breadcrumb("Home", "/"));
        
        // Add intermediate items (all linked except the last one)
        for (int i = 0; i < items.length - 1; i++) {
            breadcrumbs.add(new Breadcrumb(items[i], "#")); // You can set proper URLs
        }
        
        // Add last item as active (not linked)
        if (items.length > 0) {
            breadcrumbs.add(new Breadcrumb(items[items.length - 1]));
        }
        
        return breadcrumbs;
    }
    
    public static java.util.List<Breadcrumb> createWithUrls(Object... items) {
        java.util.List<Breadcrumb> breadcrumbs = new java.util.ArrayList<>();
        
        // Add Home as first item
        breadcrumbs.add(new Breadcrumb("Home", "/"));
        
        // Items should be pairs of (name, url) except the last one
        for (int i = 0; i < items.length - 1; i += 2) {
            if (i + 1 < items.length) {
                breadcrumbs.add(new Breadcrumb((String) items[i], (String) items[i + 1]));
            }
        }
        
        // If there's an odd number of items, the last one is active (no URL)
        if (items.length % 2 == 1) {
            breadcrumbs.add(new Breadcrumb((String) items[items.length - 1]));
        }
        
        return breadcrumbs;
    }
}
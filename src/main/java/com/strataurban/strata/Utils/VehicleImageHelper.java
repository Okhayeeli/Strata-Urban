package com.strataurban.strata.Utils;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Vehicle Image Helper
 * Maps vehicles to their local image paths with smart fallbacks
 */
@Component
public class VehicleImageHelper {

    // Base path for vehicle images
    private static final String IMAGE_BASE_PATH = "/images/vehicles/";
    
    // Brand logos path
    private static final String BRAND_LOGOS_PATH = IMAGE_BASE_PATH + "brands/";
    
    // Vehicle type images path
    private static final String TYPE_IMAGES_PATH = IMAGE_BASE_PATH + "types/";
    
    // Specific vehicle images path
    private static final String SPECIFIC_IMAGES_PATH = IMAGE_BASE_PATH + "specific/";

    // Map of specific vehicles to their images
    private static final Map<String, String> SPECIFIC_VEHICLE_IMAGES = new HashMap<>();
    
    // Map of brands to their logos
    private static final Map<String, String> BRAND_LOGOS = new HashMap<>();

    static {
        // Initialize specific vehicle images (brand-model combinations)
        initializeSpecificVehicles();
        
        // Initialize brand logos
        initializeBrandLogos();
    }

    /**
     * Get image URL for a vehicle
     * Priority: Specific Image > Type Image > Brand Logo
     */
    public String getVehicleImageUrl(String brand, String model, String type) {
        // 1. Try to find specific vehicle image (brand + model)
        String specificImage = getSpecificVehicleImage(brand, model);
        if (specificImage != null) {
            return specificImage;
        }

        // 2. Fall back to generic type image
        String typeImage = getTypeImage(type);
        if (typeImage != null) {
            return typeImage;
        }

        // 3. Final fallback to default car image
        return TYPE_IMAGES_PATH + "default-car.jpg";
    }

    /**
     * Get brand logo URL for thumbnail/list view
     */
    public String getBrandLogoUrl(String brand) {
        if (brand == null || brand.isEmpty()) {
            return BRAND_LOGOS_PATH + "default-logo.png";
        }

        String normalizedBrand = normalizeBrand(brand);
        String logoPath = BRAND_LOGOS.get(normalizedBrand);
        
        return logoPath != null ? logoPath : BRAND_LOGOS_PATH + "default-logo.png";
    }

    /**
     * Get specific vehicle image (for detail page)
     */
    private String getSpecificVehicleImage(String brand, String model) {
        if (brand == null || model == null) {
            return null;
        }

        String key = normalizeVehicleKey(brand, model);
        return SPECIFIC_VEHICLE_IMAGES.get(key);
    }

    /**
     * Get generic type image
     */
    private String getTypeImage(String type) {
        if (type == null || type.isEmpty()) {
            return null;
        }

        return switch (type.toLowerCase()) {
            case "bus" -> TYPE_IMAGES_PATH + "bus.jpg";
            case "taxi", "sedan" -> TYPE_IMAGES_PATH + "sedan.jpg";
            case "suv" -> TYPE_IMAGES_PATH + "suv.jpg";
            case "van" -> TYPE_IMAGES_PATH + "van.jpg";
            case "truck" -> TYPE_IMAGES_PATH + "truck.jpg";
            default -> TYPE_IMAGES_PATH + "default-car.jpg";
        };
    }

    /**
     * Initialize specific vehicle images
     * Add your downloaded images here
     */
    private static void initializeSpecificVehicles() {
        // Toyota
        SPECIFIC_VEHICLE_IMAGES.put("toyota-camry", SPECIFIC_IMAGES_PATH + "toyota-camry.jpg");
        SPECIFIC_VEHICLE_IMAGES.put("toyota-corolla", SPECIFIC_IMAGES_PATH + "toyota-corolla.jpg");
        SPECIFIC_VEHICLE_IMAGES.put("toyota-rav4", SPECIFIC_IMAGES_PATH + "toyota-rav4.jpg");
        SPECIFIC_VEHICLE_IMAGES.put("toyota-hilux", SPECIFIC_IMAGES_PATH + "toyota-hilux.jpg");
        SPECIFIC_VEHICLE_IMAGES.put("toyota-hiace", SPECIFIC_IMAGES_PATH + "toyota-hiace.jpg");

        // Honda
        SPECIFIC_VEHICLE_IMAGES.put("honda-civic", SPECIFIC_IMAGES_PATH + "honda-civic.jpg");
        SPECIFIC_VEHICLE_IMAGES.put("honda-accord", SPECIFIC_IMAGES_PATH + "honda-accord.jpg");
        SPECIFIC_VEHICLE_IMAGES.put("honda-cr-v", SPECIFIC_IMAGES_PATH + "honda-crv.jpg");
        SPECIFIC_VEHICLE_IMAGES.put("honda-pilot", SPECIFIC_IMAGES_PATH + "honda-pilot.jpg");

        // Ford
        SPECIFIC_VEHICLE_IMAGES.put("ford-f-150", SPECIFIC_IMAGES_PATH + "ford-f150.jpg");
        SPECIFIC_VEHICLE_IMAGES.put("ford-f150", SPECIFIC_IMAGES_PATH + "ford-f150.jpg");
        SPECIFIC_VEHICLE_IMAGES.put("ford-transit", SPECIFIC_IMAGES_PATH + "ford-transit.jpg");
        SPECIFIC_VEHICLE_IMAGES.put("ford-explorer", SPECIFIC_IMAGES_PATH + "ford-explorer.jpg");

        // Mercedes
        SPECIFIC_VEHICLE_IMAGES.put("mercedes-c-class", SPECIFIC_IMAGES_PATH + "mercedes-c-class.jpg");
        SPECIFIC_VEHICLE_IMAGES.put("mercedes-e-class", SPECIFIC_IMAGES_PATH + "mercedes-e-class.jpg");
        SPECIFIC_VEHICLE_IMAGES.put("mercedes-sprinter", SPECIFIC_IMAGES_PATH + "mercedes-sprinter.jpg");

        // BMW
        SPECIFIC_VEHICLE_IMAGES.put("bmw-3-series", SPECIFIC_IMAGES_PATH + "bmw-3-series.jpg");
        SPECIFIC_VEHICLE_IMAGES.put("bmw-x5", SPECIFIC_IMAGES_PATH + "bmw-x5.jpg");

        // Nissan
        SPECIFIC_VEHICLE_IMAGES.put("nissan-altima", SPECIFIC_IMAGES_PATH + "nissan-altima.jpg");
        SPECIFIC_VEHICLE_IMAGES.put("nissan-pathfinder", SPECIFIC_IMAGES_PATH + "nissan-pathfinder.jpg");

        // Add more as you download images...
    }

    /**
     * Initialize brand logos
     */
    private static void initializeBrandLogos() {
        BRAND_LOGOS.put("toyota", BRAND_LOGOS_PATH + "toyota.png");
        BRAND_LOGOS.put("honda", BRAND_LOGOS_PATH + "honda.png");
        BRAND_LOGOS.put("ford", BRAND_LOGOS_PATH + "ford.png");
        BRAND_LOGOS.put("mercedes", BRAND_LOGOS_PATH + "mercedes.png");
        BRAND_LOGOS.put("mercedes-benz", BRAND_LOGOS_PATH + "mercedes.png");
        BRAND_LOGOS.put("bmw", BRAND_LOGOS_PATH + "bmw.png");
        BRAND_LOGOS.put("nissan", BRAND_LOGOS_PATH + "nissan.png");
        BRAND_LOGOS.put("volkswagen", BRAND_LOGOS_PATH + "volkswagen.png");
        BRAND_LOGOS.put("vw", BRAND_LOGOS_PATH + "volkswagen.png");
        BRAND_LOGOS.put("hyundai", BRAND_LOGOS_PATH + "hyundai.png");
        BRAND_LOGOS.put("kia", BRAND_LOGOS_PATH + "kia.png");
        BRAND_LOGOS.put("mazda", BRAND_LOGOS_PATH + "mazda.png");
        BRAND_LOGOS.put("chevrolet", BRAND_LOGOS_PATH + "chevrolet.png");
        BRAND_LOGOS.put("peugeot", BRAND_LOGOS_PATH + "peugeot.png");
        
        // Add more brand logos as needed...
    }

    /**
     * Normalize vehicle key for lookup
     */
    private String normalizeVehicleKey(String brand, String model) {
        String normalizedBrand = normalizeBrand(brand);
        String normalizedModel = normalizeModel(model);
        return normalizedBrand + "-" + normalizedModel;
    }

    /**
     * Normalize brand name
     */
    private String normalizeBrand(String brand) {
        if (brand == null) return "";
        return brand.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9-]", "")
                .replaceAll("\\s+", "-");
    }

    /**
     * Normalize model name
     */
    private String normalizeModel(String model) {
        if (model == null) return "";
        return model.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9-]", "")
                .replaceAll("\\s+", "-");
    }

    /**
     * Check if specific image exists for vehicle
     */
    public boolean hasSpecificImage(String brand, String model) {
        String key = normalizeVehicleKey(brand, model);
        return SPECIFIC_VEHICLE_IMAGES.containsKey(key);
    }

    /**
     * Get fallback icon based on vehicle type
     */
    public String getVehicleIcon(String type) {
        if (type == null) return "🚗";
        
        return switch (type.toLowerCase()) {
            case "bus" -> "🚌";
            case "truck" -> "🚚";
            case "van" -> "🚐";
            case "suv" -> "🚙";
            default -> "🚗";
        };
    }
}

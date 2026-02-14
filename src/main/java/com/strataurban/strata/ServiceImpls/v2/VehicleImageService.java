package com.strataurban.strata.ServiceImpls.v2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Service to fetch real vehicle images from external APIs
 * Supports multiple image sources with fallback options
 */
@Slf4j
@Service
public class VehicleImageService {

    @Value("${vehicle.image.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final Random random = new Random();

    // Free vehicle image APIs
    private static final String UNSPLASH_API = "https://source.unsplash.com/800x600/?";
    private static final String PEXELS_SEARCH = "https://images.pexels.com/";
    private static final String LOREM_PICSUM = "https://picsum.photos/800/600?random=";
    
    // Vehicle placeholder service (custom)
    private static final String VEHICLE_PLACEHOLDER = "https://placehold.co/800x600/";

    public VehicleImageService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Get vehicle image URL based on make, model, and year
     * 
     * @param brand Vehicle brand/make (e.g., "Toyota", "Honda")
     * @param model Vehicle model (e.g., "Camry", "Civic")
     * @param year Vehicle year (e.g., "2020")
     * @param color Vehicle color (optional, for placeholder fallback)
     * @return URL to vehicle image
     */
    public String getVehicleImageUrl(String brand, String model, String year, String color) {
        try {
            // Strategy 1: Try Unsplash (best for real car photos)
            String unsplashUrl = getUnsplashVehicleImage(brand, model, year);
            if (unsplashUrl != null) {
                return unsplashUrl;
            }

            // Strategy 2: Use curated list of common vehicles
            String curatedUrl = getCuratedVehicleImage(brand, model, year);
            if (curatedUrl != null) {
                return curatedUrl;
            }

            // Strategy 3: Generic vehicle image from Unsplash
            return getGenericVehicleImage(brand, color);

        } catch (Exception e) {
            log.error("Error fetching vehicle image for {} {} {}: {}", brand, model, year, e.getMessage());
            return getPlaceholderImage(color);
        }
    }

    /**
     * Get image from Unsplash based on vehicle details
     */
    private String getUnsplashVehicleImage(String brand, String model, String year) {
        try {
            // Build search query: "brand model year car"
            String query = String.format("%s %s %s car", 
                sanitize(brand), sanitize(model), sanitize(year));
            
            // Unsplash Source API (free, no API key needed)
            return UNSPLASH_API + URLEncoder.encode(query, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.debug("Unsplash image fetch failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get curated vehicle images for popular makes/models
     * This uses a mapping of common vehicles to their stock images
     */
    private String getCuratedVehicleImage(String brand, String model, String year) {
        // Common vehicle image URLs (these are placeholders - you can update with real URLs)
        String normalizedBrand = sanitize(brand).toLowerCase();
        String normalizedModel = sanitize(model).toLowerCase();

        // You can expand this with real image URLs from stock photo sites
        return switch (normalizedBrand) {
            case "toyota" -> getToyotaImage(normalizedModel, year);
            case "honda" -> getHondaImage(normalizedModel, year);
            case "ford" -> getFordImage(normalizedModel, year);
            case "mercedes", "mercedes-benz" -> getMercedesImage(normalizedModel, year);
            case "bmw" -> getBMWImage(normalizedModel, year);
            case "nissan" -> getNissanImage(normalizedModel, year);
            case "volkswagen", "vw" -> getVWImage(normalizedModel, year);
            default -> null;
        };
    }

    /**
     * Get generic vehicle image when specific one not available
     */
    private String getGenericVehicleImage(String brand, String color) {
        // Use vehicle type based on brand
        String vehicleType = "car";
        
        String query = String.format("%s %s vehicle", sanitize(brand), vehicleType);
        return UNSPLASH_API + URLEncoder.encode(query, StandardCharsets.UTF_8);
    }

    /**
     * Get placeholder image with vehicle color
     */
    private String getPlaceholderImage(String color) {
        // Convert color to hex if it's a name
        String hexColor = colorToHex(color);
        
        // Create a nice gradient placeholder
        return VEHICLE_PLACEHOLDER + hexColor + "/1a1a1a/png?text=Vehicle";
    }

    // ==================== BRAND-SPECIFIC IMAGE METHODS ====================

    private String getToyotaImage(String model, String year) {
        return switch (model) {
            case "camry" -> "https://source.unsplash.com/800x600/?" + encode("Toyota Camry " + year);
            case "corolla" -> "https://source.unsplash.com/800x600/?" + encode("Toyota Corolla " + year);
            case "rav4" -> "https://source.unsplash.com/800x600/?" + encode("Toyota RAV4 SUV " + year);
            case "highlander" -> "https://source.unsplash.com/800x600/?" + encode("Toyota Highlander SUV " + year);
            case "4runner" -> "https://source.unsplash.com/800x600/?" + encode("Toyota 4Runner SUV " + year);
            case "tacoma" -> "https://source.unsplash.com/800x600/?" + encode("Toyota Tacoma truck " + year);
            case "tundra" -> "https://source.unsplash.com/800x600/?" + encode("Toyota Tundra truck " + year);
            case "prius" -> "https://source.unsplash.com/800x600/?" + encode("Toyota Prius hybrid " + year);
            case "sienna" -> "https://source.unsplash.com/800x600/?" + encode("Toyota Sienna van " + year);
            case "hilux" -> "https://source.unsplash.com/800x600/?" + encode("Toyota Hilux pickup " + year);
            case "hiace" -> "https://source.unsplash.com/800x600/?" + encode("Toyota HiAce van " + year);
            default -> "https://source.unsplash.com/800x600/?" + encode("Toyota " + model + " " + year);
        };
    }

    private String getHondaImage(String model, String year) {
        return switch (model) {
            case "civic" -> "https://source.unsplash.com/800x600/?" + encode("Honda Civic " + year);
            case "accord" -> "https://source.unsplash.com/800x600/?" + encode("Honda Accord " + year);
            case "cr-v", "crv" -> "https://source.unsplash.com/800x600/?" + encode("Honda CR-V SUV " + year);
            case "pilot" -> "https://source.unsplash.com/800x600/?" + encode("Honda Pilot SUV " + year);
            case "odyssey" -> "https://source.unsplash.com/800x600/?" + encode("Honda Odyssey van " + year);
            case "ridgeline" -> "https://source.unsplash.com/800x600/?" + encode("Honda Ridgeline truck " + year);
            default -> "https://source.unsplash.com/800x600/?" + encode("Honda " + model + " " + year);
        };
    }

    private String getFordImage(String model, String year) {
        return switch (model) {
            case "f-150", "f150" -> "https://source.unsplash.com/800x600/?" + encode("Ford F-150 truck " + year);
            case "mustang" -> "https://source.unsplash.com/800x600/?" + encode("Ford Mustang sports car " + year);
            case "explorer" -> "https://source.unsplash.com/800x600/?" + encode("Ford Explorer SUV " + year);
            case "escape" -> "https://source.unsplash.com/800x600/?" + encode("Ford Escape SUV " + year);
            case "expedition" -> "https://source.unsplash.com/800x600/?" + encode("Ford Expedition SUV " + year);
            case "ranger" -> "https://source.unsplash.com/800x600/?" + encode("Ford Ranger truck " + year);
            case "transit" -> "https://source.unsplash.com/800x600/?" + encode("Ford Transit van " + year);
            default -> "https://source.unsplash.com/800x600/?" + encode("Ford " + model + " " + year);
        };
    }

    private String getMercedesImage(String model, String year) {
        return switch (model) {
            case "c-class", "c class" -> "https://source.unsplash.com/800x600/?" + encode("Mercedes-Benz C-Class " + year);
            case "e-class", "e class" -> "https://source.unsplash.com/800x600/?" + encode("Mercedes-Benz E-Class " + year);
            case "s-class", "s class" -> "https://source.unsplash.com/800x600/?" + encode("Mercedes-Benz S-Class " + year);
            case "gle" -> "https://source.unsplash.com/800x600/?" + encode("Mercedes-Benz GLE SUV " + year);
            case "glc" -> "https://source.unsplash.com/800x600/?" + encode("Mercedes-Benz GLC SUV " + year);
            case "gls" -> "https://source.unsplash.com/800x600/?" + encode("Mercedes-Benz GLS SUV " + year);
            case "sprinter" -> "https://source.unsplash.com/800x600/?" + encode("Mercedes-Benz Sprinter van " + year);
            default -> "https://source.unsplash.com/800x600/?" + encode("Mercedes-Benz " + model + " " + year);
        };
    }

    private String getBMWImage(String model, String year) {
        return switch (model) {
            case "3 series", "3-series" -> "https://source.unsplash.com/800x600/?" + encode("BMW 3 Series " + year);
            case "5 series", "5-series" -> "https://source.unsplash.com/800x600/?" + encode("BMW 5 Series " + year);
            case "7 series", "7-series" -> "https://source.unsplash.com/800x600/?" + encode("BMW 7 Series " + year);
            case "x3" -> "https://source.unsplash.com/800x600/?" + encode("BMW X3 SUV " + year);
            case "x5" -> "https://source.unsplash.com/800x600/?" + encode("BMW X5 SUV " + year);
            case "x7" -> "https://source.unsplash.com/800x600/?" + encode("BMW X7 SUV " + year);
            default -> "https://source.unsplash.com/800x600/?" + encode("BMW " + model + " " + year);
        };
    }

    private String getNissanImage(String model, String year) {
        return switch (model) {
            case "altima" -> "https://source.unsplash.com/800x600/?" + encode("Nissan Altima " + year);
            case "maxima" -> "https://source.unsplash.com/800x600/?" + encode("Nissan Maxima " + year);
            case "rogue" -> "https://source.unsplash.com/800x600/?" + encode("Nissan Rogue SUV " + year);
            case "pathfinder" -> "https://source.unsplash.com/800x600/?" + encode("Nissan Pathfinder SUV " + year);
            case "frontier" -> "https://source.unsplash.com/800x600/?" + encode("Nissan Frontier truck " + year);
            case "titan" -> "https://source.unsplash.com/800x600/?" + encode("Nissan Titan truck " + year);
            case "sentra" -> "https://source.unsplash.com/800x600/?" + encode("Nissan Sentra " + year);
            case "versa" -> "https://source.unsplash.com/800x600/?" + encode("Nissan Versa " + year);
            default -> "https://source.unsplash.com/800x600/?" + encode("Nissan " + model + " " + year);
        };
    }

    private String getVWImage(String model, String year) {
        return switch (model) {
            case "golf" -> "https://source.unsplash.com/800x600/?" + encode("Volkswagen Golf " + year);
            case "jetta" -> "https://source.unsplash.com/800x600/?" + encode("Volkswagen Jetta " + year);
            case "passat" -> "https://source.unsplash.com/800x600/?" + encode("Volkswagen Passat " + year);
            case "tiguan" -> "https://source.unsplash.com/800x600/?" + encode("Volkswagen Tiguan SUV " + year);
            case "atlas" -> "https://source.unsplash.com/800x600/?" + encode("Volkswagen Atlas SUV " + year);
            default -> "https://source.unsplash.com/800x600/?" + encode("Volkswagen " + model + " " + year);
        };
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Sanitize string for use in URLs
     */
    private String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        return input.trim().replaceAll("[^a-zA-Z0-9\\s-]", "");
    }

    /**
     * URL encode string
     */
    private String encode(String input) {
        try {
            return URLEncoder.encode(input, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return input;
        }
    }

    /**
     * Convert color name to hex code
     */
    private String colorToHex(String colorName) {
        if (colorName == null || colorName.isEmpty()) {
            return "667eea"; // Default purple
        }

        // Check if already hex
        if (colorName.matches("^#?[0-9A-Fa-f]{6}$")) {
            return colorName.replace("#", "");
        }

        // Convert common color names to hex
        return switch (colorName.toLowerCase().trim()) {
            case "red" -> "ef4444";
            case "blue" -> "3b82f6";
            case "green" -> "10b981";
            case "yellow" -> "f59e0b";
            case "black" -> "1a1a1a";
            case "white" -> "f9fafb";
            case "gray", "grey" -> "6b7280";
            case "silver" -> "9ca3af";
            case "orange" -> "f97316";
            case "purple" -> "8b5cf6";
            case "pink" -> "ec4899";
            case "brown" -> "92400e";
            case "gold" -> "fbbf24";
            default -> "667eea"; // Default
        };
    }

    /**
     * Get fallback image URL
     */
    public String getFallbackImageUrl() {
        return "https://source.unsplash.com/800x600/?car,vehicle,automobile";
    }
}

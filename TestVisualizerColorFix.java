import com.musicplayer.services.SettingsService;
import com.musicplayer.data.models.Settings;

public class TestVisualizerColorFix {
    public static void main(String[] args) {
        System.out.println("Testing visualizer color mode fix...");
        
        // Create a new SettingsService instance
        SettingsService settingsService = new SettingsService();
        Settings settings = settingsService.getSettings();
        
        System.out.println("Current visualizer color mode: " + settings.getVisualizerColorMode());
        System.out.println("Expected: SOLID_COLOR (from settings.json)");
        
        if (settings.getVisualizerColorMode() == Settings.VisualizerColorMode.SOLID_COLOR) {
            System.out.println("✅ SUCCESS: Visualizer color mode is correctly read from settings.json");
        } else {
            System.out.println("❌ FAILED: Visualizer color mode is not reading from settings.json");
        }
    }
}

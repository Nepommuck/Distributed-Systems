package server;

public interface Config {
    String IP_ADDRESS = "127.0.0.5";
    int PORT = 50051;

    float MIN_BRIGHTNESS_LEVEL = 0;
    float MAX_BRIGHTNESS_LEVEL = 100;

    float MIN_TEMPERATURE_K = 1_500;
    float MAX_TEMPERATURE_K = 10_000;

    float MIN_FRIDGE_TEMPERATURE_C = 1;
    float MAX_FRIDGE_TEMPERATURE_C = 20;

    float MIN_FREEZER_TEMPERATURE_C = -50;
    float MAX_FREEZER_TEMPERATURE_C = 0;
}

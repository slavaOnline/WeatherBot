import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class NewBot extends TelegramLongPollingBot {

    HttpClient client;

    public NewBot() {
        client = HttpClient.newHttpClient();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chat_id = update.getMessage().getChatId();

            SendMessage message = new SendMessage();

            message.setChatId(chat_id);

            try {
                String tempForMessage = getWeather();
                message.setText(tempForMessage);
            } catch (IOException | URISyntaxException | InterruptedException e) {
                message.setText("Какие-то проблемы у бота, обратитесь ко мне через сутки, я болею ;(");
            }

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public String getBotUsername() {
        return "WeatherCheckerStudyBot";
    }

    @Override
    public String getBotToken() {
        return "";
    }

    private String getWeather() throws IOException, InterruptedException, URISyntaxException {

        String apiKey = "";

        String host = "http://dataservice.accuweather.com";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(host + "/locations/v1/cities" + "/search?apikey=" + apiKey + "&q=Irkutsk"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        String cityKey = mapper.readTree(response.body()).get(0).at("/Key").toString().replaceAll("\"", "");

        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(new URI(host + "/forecasts/v1/daily/1day/" + cityKey + "?apikey=" + apiKey + "&metric=true"))
                .GET()
                .build();

        HttpResponse<String> response2 = client.send(request2,
                HttpResponse.BodyHandlers.ofString());

        JsonNode nodeOfTemperature = mapper.readTree(response2.body()).at("/DailyForecasts").get(0).at("/Temperature");
        String temperatureMin = nodeOfTemperature.at("/Minimum").at("/Value").toString();
        String temperatureMax = nodeOfTemperature.at("/Maximum").at("/Value").toString();
        return "Минимальная температура за день: " + temperatureMin + "\n" + "Максимальная температура за день: " + temperatureMax;
    }

}
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;




public class HttpUtil {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Использование: currency_rates --code=USD --date=2022-10-08");
            return;
        }

        String currencyCode = null;
        String dateStr = null;

        for (String arg : args) {
            if (arg.startsWith("--code=")) {
                currencyCode = arg.substring(7);
            } else if (arg.startsWith("--date=")) {
                dateStr = arg.substring(7);
            }
        }

        if (currencyCode == null || dateStr == null) {
            System.out.println("Необходимо указать код валюты и дату.");
            return;
        }

        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = inputDateFormat.parse(dateStr);
            String formattedDate = new SimpleDateFormat("dd/MM/yyyy").format(date);
            getCurrencyRate(currencyCode, formattedDate);
        } catch (ParseException e) {
            System.out.println("Неверный формат даты. Используйте формат YYYY-MM-DD");
        }
    }

    private static void getCurrencyRate(String currencyCode, String formattedDate) {
        String apiUrl = "https://www.cbr.ru/scripts/XML_daily.asp?date_req=" + formattedDate;
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(url.openStream());
            doc.getDocumentElement().normalize();

            NodeList valuteList = doc.getElementsByTagName("Valute");
            for (int i = 0; i < valuteList.getLength(); i++) {
                Element valute = (Element) valuteList.item(i);
                String charCode = valute.getElementsByTagName("CharCode").item(0).getTextContent();
                if (charCode.equalsIgnoreCase(currencyCode)) {
                    String name = valute.getElementsByTagName("Name").item(0).getTextContent();
                    String value = valute.getElementsByTagName("Value").item(0).getTextContent();
                    DecimalFormat decimalFormat = new DecimalFormat("#.####");
                    System.out.println(currencyCode + " (" + name + "): " + decimalFormat.format(Double.parseDouble(value.replace(",", "."))));
                    return;
                }
            }

            System.out.println("Валюта с кодом " + currencyCode + " не найдена.");
        } catch (IOException e) {
            System.out.println("Ошибка при получении данных с сервера: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Ошибка при обработке XML данных: " + e.getMessage());
        }
    }
}
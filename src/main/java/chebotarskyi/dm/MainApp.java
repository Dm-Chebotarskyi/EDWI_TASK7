package chebotarskyi.dm;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MainApp {

    private static int count = 1;
    private final static int threshold = 500;

    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();

        String rootURL = args[0];

        Set<String> links = new HashSet<>();

        Set<String> innerLinks = process(rootURL);

        while (count < threshold) {
            if (innerLinks == null) {
                break;
                //TODO: Figure out what to do with null
            }

            Set<String> linksToProcess = new HashSet<>(innerLinks);

            linksToProcess.removeAll(links);

            links.addAll(innerLinks);
            if (linksToProcess.size() == 0)
                break;
            for (String link : linksToProcess) {

                if (count >= threshold)
                    break;

                Set<String> newLinks = process(link);
                if (newLinks != null)
                    innerLinks.addAll(newLinks);
            }
        }


        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.print("Processed " + count + " links in " +
                String.format("%02d min, %02d sec",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
        ));
    }

    private static Set<String> process(String link) {

        String rootDomain = getDomainName(link);

        try {
            Element body = getBodyFrom(link);

            if (body != null) {

                System.out.println(link);
                index(body);

                Set<String> innerLinks = getLinksFrom(body);
                Set<String> links = new HashSet<>();
                for (String l : innerLinks) {
                    if (l.contains(".pdf") || l.contains(".mp4") || l.contains(".mpeg4"))
                        continue;

                    if (getDomainName(l).equals("")) {
                        try {
                            URL url = new URL(new URL("http://" + rootDomain + "/"), l);
                            links.add(url.toString());
                        } catch (Exception e) {
                        }
                    } else {
                        links.add(l);
                    }
                }

                return links;
            }

        } catch (IOException e) {
        }

        return null;
    }

    private static void index(Element body) {
        System.out.print(count);
        System.out.println(" Processed");
        count++;
    }

    private static Element getBodyFrom(String url) throws IOException {
        Document html;
        try {
            if (!url.contains("http"))
                url = "http://" + url;
            html = Jsoup.connect(url).get();
        } catch (IllegalArgumentException e) {
            System.out.println("Malformed url!");
            return null;
        }
        Elements elements = html.getElementsByTag("body");
        if (elements.size() > 0)
            return elements.get(0);
        else
            return null;
    }

    private static Set<String> getLinksFrom(Element element) {
        Set<String> links = new HashSet<>();

        Elements elements = element.getElementsByAttribute("href");
        for (Element e : elements) {
            links.add(e.attr("href"));
        }

        return links;
    }

    public static String getDomainName(String url) {
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            return "";
        }
        String domain = uri.getHost();
        if (domain != null)
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        return "";
    }

}

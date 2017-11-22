package chebotarskyi.dm;

import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;

public class MainApp {

    private static int count = 1;

    public static void main(String[] args) {

        String rootURL = args[0];
        int threshold = Integer.parseInt(args[1]);

//        IndexUtils indexUtils = new IndexUtils();
//
//        LinkProcessor linkProcessor = new LinkProcessor(threshold, indexUtils);
//        linkProcessor.startProcessing(rootURL);

//        indexUtils.closeWriter();

        QueryProcessor queryProcessor = new QueryProcessor();
            queryProcessor.startQueryProcessor();
    }


}

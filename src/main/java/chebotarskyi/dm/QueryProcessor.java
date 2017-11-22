package chebotarskyi.dm;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.packed.PackedInts;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.Scanner;


public class QueryProcessor {

    private final StandardAnalyzer analyzer;
    private FSDirectory index;

    public QueryProcessor() {
        analyzer = new StandardAnalyzer();
        try {
            index = FSDirectory.open(Paths.get(IndexUtils.INDEX_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startQueryProcessor() {

        while (true) {

            String querystr = askForQuery();
            if (querystr.equals("-1")) {
                System.out.println("Bye!");
                break;
            }
            Query q = null;
            try {
                q = new QueryParser("body", analyzer).parse(querystr);
                processQuery(q);
            } catch (Exception e) {
                System.out.println("Wrong query!");
                System.out.println(e.getMessage());
            }
        }
    }

    private String askForQuery() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n\nPlease enter query to search: ");
        String title = scanner.nextLine();
        return title;
    }

    private void processQuery(Query q) throws IOException {
        int hitsPerPage = 5;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;


        System.out.println("Found " + hits.length + " hits.");
        for (int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);

            System.out.println((i + 1) + ". " + d.get("url") + "\n");
        }

        MoreLikeThis mlt = new MoreLikeThis(reader);
        mlt.setFieldNames(new String[] {"url", "body"});
        mlt.setAnalyzer(analyzer);

        if (hits.length > 0) {
            TopDocs topDocs = searcher.search(mlt.like(hits[0].doc), 5);
            for (int i = 0; i < topDocs.totalHits && i < 5; ++i) {
                int docId = topDocs.scoreDocs[i].doc;
                Document d = searcher.doc(docId);

                System.out.println("Like this: " + (i + 1) + ". " + d.get("url") + "\n");
            }
        }
    }

}

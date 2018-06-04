import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatDocValuesField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class Indexer {

	private static final String ROOT_DIR = "/Users/aokireiko/Downloads/thunews/20180518071328/mirror/news.tsinghua.edu.cn";
	private static final String PAGERANK_FILE = "/Users/aokireiko/study/18_spring/搜索引擎技术/search_engine/rank.txt";
	private static final String INDEX_DIR = "forIndex/index";
	private Analyzer analyzer;
    private IndexWriter indexWriter;
    private float titleAvgLength = 1.0f;
    private float contentAvgLength = 1.0f;
    private int count = 0;
	public Indexer() {
		IKAnalyzer analyzer = new IKAnalyzer();
        try {
        	IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40,
                    analyzer);
            Directory dir = FSDirectory.open(new File(INDEX_DIR));
            indexWriter = new IndexWriter(dir, iwc);
            // indexWriter.setSimilarity(new BM25Similarity());
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	public void saveGlobals(String filename) {
        try {
        	
            PrintWriter pw = new PrintWriter(new File(filename));
            pw.println(titleAvgLength);
            pw.println(contentAvgLength);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	public void parseHtml(File file, Document doc) {
		try {
			org.jsoup.nodes.Document jsoupDoc  = Jsoup.parse(file,"UTF-8","http://www.oschina.net/");
			String title = jsoupDoc.select("title").text();
			String content = jsoupDoc.select("article").select("p,h1,h2,strong").text();
			Elements elements = jsoupDoc.select("meta[name=keywords]");
			
			Field titleField = new TextField("title", title, Field.Store.YES);
			Field contentField = new TextField("content", content, Field.Store.YES);
				
			doc.add(titleField);
			doc.add(contentField);
			String keywords = "";
			if (elements.size() > 0) {
				keywords = jsoupDoc.select("meta[name=keywords]").get(0).attr("content");
			}
			Field keyField = new TextField("keywords", keywords, Field.Store.YES);
			doc.add(keyField);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Miss "+file.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	public void parsePdf(File file, Document doc) {
		try {
			
			PDFParser parser = new PDFParser(new RandomAccessFile(file, "r"));
			parser.parse();
			PDDocument pd = parser.getPDDocument();
			PDFTextStripper tStripper = new PDFTextStripper();
			String content = tStripper.getText(pd);
			String title = file.getName();
			pd.close();
			
			Field titleField = new TextField("title", title, Field.Store.YES);
			Field contentField = new TextField("content", content, Field.Store.YES); 
			
			doc.add(titleField);
			doc.add(contentField);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Miss "+file.getAbsolutePath());
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	public void parseDoc(File file, Document doc) {
		try {
			WordExtractor extractor = new WordExtractor(new FileInputStream(file));
			String content = extractor.getText();
			extractor.close();
			String title = file.getName();
			
			Field titleField = new TextField("title", title, Field.Store.YES);
			Field contentField = new TextField("content", content, Field.Store.YES); 
			
			doc.add(titleField);
			doc.add(contentField);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Miss "+file.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void parseDocx(File file, Document doc) {
		try {
			XWPFWordExtractor docx = new XWPFWordExtractor(
					new XWPFDocument(POIXMLDocument.openPackage(file.getAbsolutePath())));
	        String content = docx.getText();
	        docx.close();
			String title = file.getName();
			
			Field titleField = new TextField("title", title, Field.Store.YES);
			Field contentField = new TextField("content", content, Field.Store.YES); 
			
			doc.add(titleField);
			doc.add(contentField);
	        
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Miss "+file.getAbsolutePath());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
        
	}
	public void build_index() {
		try {
			InputStreamReader read = new InputStreamReader(
	                new FileInputStream(new File(PAGERANK_FILE)), "utf8");// 考虑到编码格式
	        BufferedReader bufferedReader = new BufferedReader(read);
	        String lineTxt = null;
	        while ((lineTxt = bufferedReader.readLine()) != null)
            {
	        	String[] items = lineTxt.split(" ");
	        	String name = items[0];
	        	float rank = Float.parseFloat(items[1]);
	        	File page;
	        	try {
					page = new File(ROOT_DIR+name);
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println("Index find no file "+name);
					continue;
				}
	        	Document doc = new Document();
	        	if (name.endsWith(".html") || name.endsWith("htm"))
	        		parseHtml(page, doc);
	        	if (name.endsWith(".pdf"))
	        		parsePdf(page, doc);
	        	if (name.endsWith(".doc"))
	        		parseDoc(page, doc);
	        	if (name.endsWith(".docx"))
	        		parseDocx(page, doc);
	    
	        	Field rankField = new FloatDocValuesField("pagerank", rank);
	        	doc.add(rankField);
	        	Field pathField = new Field("path", name, Field.Store.YES, Field.Index.NO);
	        	doc.add(pathField);
	        	
	        	count += 1;
	        	if (count % 100 == 0) System.out.println("processed "+count);
	        	// TODO: 判断是否index成功
	        	indexWriter.addDocument(doc);
            }
	        indexWriter.close();
	        
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println("Can't find the page rank file " + PAGERANK_FILE);
		}
		
		
		
	}
	public static void main(String[] args) {
		Indexer indexer = new Indexer();
		indexer.build_index();
		/*分词测试
		IKAnalyzer analyzer = new IKAnalyzer();
		//new IKAnalyzer(useSmart, useSingle, useItself)
		TokenStream tStream;
		try {
			tStream = analyzer.tokenStream("title", new StringReader("test 林校长今日莅临指导"));
			tStream.reset();
			while(tStream.incrementToken()) {
				String token = tStream.getAttribute(CharTermAttribute.class).toString();
				System.out.println(token);
				
			}
			tStream.end();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/
		System.out.println("over");
	}

}

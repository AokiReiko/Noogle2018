import java.io.*;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.wltea.analyzer.lucene.IKAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;


public class Indexer {
	// TODO:hard code
	static final String INDEX_FILE_NAME = "forIndex/index";
	static final String PAGE_ROOT_DIR = "D:/tsinghua/20180518172144/mirror/news.tsinghua.edu.cn";
	static final String PAGE_RANK_FILE = "rank.txt";	
	
	private Analyzer analyzer; 
    private IndexWriter indexWriter;
    HashMap<String, Float> pageRank = new HashMap<String, Float>();
    private int cnt = 0;
    
    public Indexer(String indexDir){
    	analyzer = new IKAnalyzer();
    	try{
    		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);
    		Directory dir = FSDirectory.open(new File(indexDir));
    		indexWriter = new IndexWriter(dir,iwc);
    		
    		System.out.println("Load in pagerank");
    		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(PAGE_RANK_FILE)));
	        for (String line = br.readLine(); line != null; line = br.readLine()) 
	        {
	        	String url = line.substring(0, line.indexOf(' '));
	        	String pagerank = line.substring(line.indexOf(' ')+1);
	        	float pr = Float.parseFloat(pagerank);
	        	pageRank.put(url, pr);
//	        	System.out.println(url + ", " + pr);
	        }
	        br.close();
	        
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }
    
    public String readPdf(File file)
	{
		PDFTextStripper pdfStripper = null;
	    PDDocument pdDoc = null;
	    COSDocument cosDoc = null;
	    String parsedText = null;
	    try {
	        PDFParser parser = new PDFParser(new FileInputStream(file));
	        parser.parse();
	        cosDoc = parser.getDocument();
	        pdfStripper = new PDFTextStripper();
	        pdDoc = new PDDocument(cosDoc);
	        pdfStripper.setStartPage(1);
	        pdfStripper.setEndPage(pdDoc.getNumberOfPages());
	        parsedText = pdfStripper.getText(pdDoc);
	        pdDoc.close();
	        cosDoc.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	        } 
	    return parsedText;
    }
    
	public void indexDocs(File file){
		try{
			if(file.canRead()){
				if(file.isDirectory()){
					String[] files = file.list();
					if(files != null){
						for (int i = 0; i < files.length; i++){
							indexDocs(new File(file, files[i]));
						}
					}
				} else {
					FileInputStream fis;
					try{
						fis = new FileInputStream(file);
					} catch (FileNotFoundException fnfe) {
						return;
					}
					try{     
						if(file.getName().endsWith("html")) { 
							org.jsoup.nodes.Document docc = Jsoup.parse(file, "UTF-8","");
							Document document = new Document();
							StringField pathField = new StringField("path", file.getPath(), Field.Store.YES);
							Field titleField = new TextField("title", docc.select("title").text(), Field.Store.YES);
							Field keywordField = new TextField("keyword", docc.select("h1,h2,h3,h4,h5,h6,b,strong").text(), Field.Store.YES);
							Field contentField  = new TextField( "content" ,docc.select("p").text(), Field.Store.YES);
							Field linkField = new TextField("link", docc.select("a").text(), Field.Store.YES);
							
							String relative_path = file.getPath().replace(PAGE_ROOT_DIR, "").replace('\\', '/');
//							System.out.println(relative_path);
							//multiply all fields to get a document boost
							try{
								float pr = pageRank.get(relative_path);
								pr = (float) (16 + Math.log(pr));
								titleField.setBoost(pr);
								keywordField.setBoost(pr);
								contentField.setBoost(pr);
								linkField.setBoost(pr);
							} catch(Exception e)  {
								System.out.println("no page rank for:"+relative_path);
								e.printStackTrace();
							}
							document.add(pathField);
							document.add(titleField);
							document.add(keywordField);
							document.add(contentField);
							document.add(linkField);
							indexWriter.addDocument(document);
							cnt += 1;
							if(cnt % 100 == 0)
								System.out.println(cnt);				
						} else if(file.getName().endsWith("pdf") | file.getName().endsWith("doc")){
							String text = null;
							try{
								if(file.getName().endsWith("pdf"))
									text = readPdf(file);
								else if(file.getName().endsWith("doc")){
									HWPFDocument docx = new HWPFDocument( new FileInputStream(file));
									WordExtractor we = new WordExtractor(docx);
									text = we.getText();
									we.close(); 
								}
								Document document = new Document();
								Field pathField = new StringField("path", file.getPath(), Field.Store.YES);
								Field titleField = new TextField("title", file.getName(), Field.Store.YES);
								Field keywordField = new TextField("keyword", "", Field.Store.YES);
								Field contentField  = new TextField( "content" , text, Field.Store.YES);
								Field linkField = new TextField("link", "", Field.Store.YES);
								
								float pr = 0.0012381792195339148f;	
								pr = (float) (16 + Math.log(pr));
								titleField.setBoost(pr);
								keywordField.setBoost(pr);
								contentField.setBoost(pr);
								linkField.setBoost(pr);
								document.add(pathField);
								document.add(titleField);
								document.add(keywordField);
								document.add(contentField);
								document.add(linkField);
								indexWriter.addDocument(document);
								cnt += 1;
							} catch(Exception e)  {
								System.out.println("Error when doing file:"+file.getPath());
								e.printStackTrace();
							}
						} else if(file.getName().endsWith("docx") | file.getName().endsWith("xls") | file.getName().endsWith("xlsx")) {
							System.out.println(file.getPath());
							System.out.println("Unhandled!");
							System.exit(0);
						}
						
					} finally {
						fis.close();
					}
				}
			}    
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void closeWriter(){
		try{ indexWriter.close();}
		catch(Exception e)	{ e.printStackTrace();}
	}
	
	public static void main(String[] args) {
		Indexer indexer=new Indexer(INDEX_FILE_NAME);
		final File docDir = new File(PAGE_ROOT_DIR);
		indexer.indexDocs(docDir);
		indexer.closeWriter();
		
		System.out.println(indexer.cnt);
	}
}

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.suggest.fst.FSTCompletion;
import org.apache.lucene.search.suggest.fst.FSTCompletionBuilder;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class Searcher {
	// TODO:hard code
	final static String RELATE_RECOMMEND_FILE = "D:\\SE\\SixerSearcher\\relate.txt";
	
	
	private IndexReader reader;
	private IndexSearcher searcher;
	private Analyzer analyzer;
	private SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("<em>", "</em>");
	private Highlighter highlighter = null;
	RelateRecommend relateRecommend = new RelateRecommend();
	FSTCompletion fstCompletion = null;
	
	@SuppressWarnings("deprecation")
	public Searcher(String indexdir){
		analyzer = new IKAnalyzer();
		try{
			System.out.println("Initialzing");
			reader = IndexReader.open(FSDirectory.open(new File(indexdir)));
			searcher = new IndexSearcher(reader);
			searcher.setSimilarity(new BM25Similarity());
			
//			// 扩展：自动补全
	        FSTCompletionBuilder fstb = new FSTCompletionBuilder();
			TermsEnum termEnum = MultiFields.getTerms(reader, "content").iterator(null);
			int cnt = 0; 
			while(termEnum.next() != null)
			{
				cnt = cnt + 1;
				if(cnt % 10000 == 0)  
					System.out.println(cnt);

				DocsEnum docEnum = MultiFields.getTermDocsEnum(reader, MultiFields.getLiveDocs(reader), "content", termEnum.term());
				int doc;
				while((doc = docEnum.nextDoc())!= DocsEnum.NO_MORE_DOCS ){
					// 得到TF,IDF信息
					Tools.add( termEnum.term().utf8ToString(), docEnum.freq(), doc);
					
					int weight = termEnum.docFreq();
					weight = FSTWeightConvert(weight);	
					fstb.add(termEnum.term(), weight);
				}
			}  
			System.out.println("Handle terms finish!\n");
			/* 相关推荐：第一次运行进行初始化（利用Utils信息），然后保存到文件
			 之后运行就直接从文件中加载 */
			
			// 第一次运行
			//sixerRelateRecommend.initAndSave(this, reader, searcher, RELATE_RECOMMEND_FILE);
			
			// 之后运行
			System.out.println("loading recommend...");
			relateRecommend.load(RELATE_RECOMMEND_FILE);
			System.out.println("load recommend finish.\n");
			
			System.out.println("buliding completion...");
			fstCompletion = fstb.build();
			System.out.println("bulid completion finish.\n");
			
			// 纠错
//			sixerCorrection.init();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getCompletion(String querystring){

		System.out.print("AutoComplete: [");
		ArrayList<String> rst = new ArrayList<String>();
		List<FSTCompletion.Completion> c = fstCompletion.lookup(querystring, 6);	
		for(FSTCompletion.Completion a : c) {
			System.out.print(a.utf8.utf8ToString()+ ", ");
			rst.add(a.utf8.utf8ToString());
		}
		System.out.print("]\n");
		return rst;
	}
	
	
	public ArrayList<String> getRelateRecommend(String querystring){
		ArrayList<String> result = relateRecommend.find(querystring, 8);
		System.out.println("RelateRecommend: "+ result);
		return result;	
	}
	
	public TopDocs searchQuery(String queryString, int maxnum){
		try {
			String [] fields = new String[] {"title", "keyword", "content", "link"};
			Map<String, Float> boosts = new HashMap<String, Float>();
			boosts.put("title", 1.0f);
			boosts.put("keyword", 1.0f);
			boosts.put("content", 5.0f);
			boosts.put("link", 100.0f);
			QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_40, fields, analyzer, boosts);
			Query query = parser.parse(queryString);
			highlighter = new Highlighter(htmlFormatter, new QueryScorer(query)); 
			TopDocs results = searcher.search(query, maxnum);
			   
			return results;   
		} catch (Exception e) {
			
		}
		return null;  
	}
	  
	public Document getDoc(int docID){
		try{
			return searcher.doc(docID);
		}catch(IOException e){
			e.printStackTrace();
		}
		return null;
	}
	
	
	public String getDecoratedTitle(int docID) { 
		// See if the title match the query
		TokenStream tokenStream = null;
		try {
			tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), docID, "title", analyzer);
			String title_matched = highlighter.getBestFragments(tokenStream, searcher.doc(docID).get("title"), 1, "...");
			if(title_matched.length() > 0){
				return title_matched;
			} else {
				return searcher.doc(docID).get("title");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
		
	public String getDecoratedDescription(int docID) {
		try{
			// Try to get matched text from Field content and Field Link, if impossible, just use content
			TokenStream tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), docID, "content", analyzer);
			String content = highlighter.getBestFragments(tokenStream, searcher.doc(docID).get("content"), 1, "...");

			TokenStream tokenStream2 = TokenSources.getAnyTokenStream(searcher.getIndexReader(), docID, "link", analyzer);
			String link = highlighter.getBestFragments(tokenStream2, searcher.doc(docID).get("link"), 1, "...");
			
			if(content.length()>0)
				return content;
			else if(link.length()>0)
				return link;
			else if(content.length()+link.length()==0)
				return searcher.doc(docID).get("content").substring(0, Math.min(searcher.doc(docID).get("content").length(), 100));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}	
	
	private static int FSTWeightConvert(int weight) {	
		int[] convertors = {400, 200, 100, 50, 40, 30, 20, 10};
		int convertedWeight = 9;
		for (int convertor : convertors) {
			if(weight > convertor) break;
			-- convertedWeight;
		}
		return convertedWeight;
	}
	
	public static void main(String[] args) throws Exception{
		


	}		
}

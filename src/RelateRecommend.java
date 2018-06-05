import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class RelateRecommend {
	
	final int MAX_RECOMMEND_NUM = 8;
	final double RELATE_THRESHOLD = 0.05;
	// 记录是否已经计算过某个word
	Set<String> handledWordSet =new HashSet<String>();
	// 键是一个word，值是按照相关性排列的wordB列表
	HashMap<Integer, ArrayList<Integer>> relate = new HashMap<Integer, ArrayList<Integer>>();
	
	
	ArrayList<String> find(String word, int num) {
		if (!Tools.s2i.containsKey(word)) {
			// 之前从未遇见过的字符串
			return new ArrayList<String>();
		}
		int wordId = Tools.s2i.get(word);
		if(!relate.containsKey(wordId)) return new ArrayList<String>();
		ArrayList<Integer> relateIdList = relate.get(wordId);
		ArrayList<String> relateList = new ArrayList<String>();
		for (int i = 0; i < Math.min(num, relateIdList.size()); ++i) {
			relateList.add(Tools.i2s.get(relateIdList.get(i)));
		}
		return relateList;
	}
	
	void initAndSave(Searcher search, IndexReader reader, IndexSearcher searcher, String filePath) {
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath)));
		
			TermsEnum termEnum = MultiFields.getTerms(reader, "content").iterator(null);
			int cnt = 0; 
			while(termEnum.next() != null) {
				cnt = cnt + 1;
				if(cnt % 1000 == 0)  {
					System.out.println(cnt);
				}

				DocsEnum docEnum = MultiFields.getTermDocsEnum(reader, MultiFields.getLiveDocs(reader), "content", termEnum.term());
				while(docEnum.nextDoc() != DocsEnum.NO_MORE_DOCS ){
					String word = termEnum.term().utf8ToString();
					if(handledWordSet.contains(word)) {
						continue;
					}
					if(!Tools.s2i.containsKey(word)) continue;
					handledWordSet.add(word);
					TopDocs results=search.searchQuery(word, 5);
					if (results == null) {
						continue;
					}
					ScoreDoc[] hits = results.scoreDocs;
					if (hits == null) continue;
					HashMap<String, Double> frequency = new HashMap<String, Double>();
					for (int i = 0; i < hits.length; i++) {
						Document document = searcher.doc(hits[i].doc);
				        
				        @SuppressWarnings("resource")
						Analyzer analyzer=new IKAnalyzer(true);
						StringReader stringReader=new StringReader(document.get("content"));
						TokenStream ts=analyzer.tokenStream("content", stringReader);
						
						while(ts.incrementToken()) {
							String wordB =ts.getAttribute(CharTermAttribute.class).toString(); 
							double wordBFreq = 0.0;
							if(frequency.containsKey(wordB)) wordBFreq = frequency.get(wordB); 
				            frequency.put(wordB, wordBFreq + 1);  
				        }  
						stringReader.close();
					} 
					if(frequency.size() < 1) continue;
					// 加上IDF
					for (Entry<String, Double> wordEntry : frequency.entrySet()) {
						String wordB = wordEntry.getKey();
						double wordBFreq = 0.0;
						if(Tools.s2i.containsKey(wordB)) { 
							wordBFreq = frequency.get(wordB) / Math.sqrt(2.0 * Tools.df.get(Tools.s2i.get(wordB)));
						}
						if(wordB.equals("的") || wordB.equals("and") || wordB.equals("of")) wordBFreq = 0.0;
						frequency.put(wordB, wordBFreq);
					}
					// 排序
					List<Entry<String,Double>> list = new ArrayList<Entry<String,Double>>(frequency.entrySet());
					Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
					    public int compare(Map.Entry<String, Double> o1,
					            Map.Entry<String, Double> o2) {
					    	Double d1 = o1.getValue();
							Double d2 = o2.getValue();
							return d2.compareTo(d1);
					    }
					});
					// 转化成文件格式
					int recommendCount = 0; 
					String line = word + " " + Math.min(MAX_RECOMMEND_NUM, list.size()) + " ";
					for(Entry<String,Double> wordEntry : list) {
						if(++recommendCount > MAX_RECOMMEND_NUM) break;
						line += (wordEntry.getKey() + " ");
					}
					// 写入文件
					writer.write(line);
					writer.write("\n");
				}
			}
			// 关闭文件
			writer.close(); 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void load(String filePath) {
		// 一个wordA对应一行
		// 每一行的格式 wordA wordBs.length wordBs[0] wordBs[1] ...
		System.out.println("In Load(" + filePath + ")");
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
	        for (String line = br.readLine(); line != null; line = br.readLine()) 
	        {
//				System.out.println(line);
				String[] lineArray = line.split(" ");
				String word = lineArray[0];
				if(!Tools.s2i.containsKey(word)) continue;
				int wordId = Tools.s2i.get(word);
//				System.out.println(word + ": " + lineArray[2]);
				relate.put(wordId, new ArrayList<Integer>());
				for (int i = 2; i < lineArray.length; ++i) {
					String word2 = lineArray[i];
					if(Tools.s2i.containsKey(word2)) {
						int word2Id = Tools.s2i.get(word2);
						relate.get(wordId).add(word2Id);
					}
				}
			}
	        br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

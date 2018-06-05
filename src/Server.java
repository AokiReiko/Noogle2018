import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import com.google.gson.Gson;
import java.util.*;
import java.net.URLEncoder;

public class Server extends HttpServlet{
	// TODO:hard code
	public static final String INDEX_ABSOLUTE_PATH = "D:\\SE\\SixerSearcher\\forIndex";
	static final String PAGE_ROOT_DIR = "D:\\tsinghua\\20180518172144\\mirror\\news.tsinghua.edu.cn";
	
	public static final int PAGE_RESULT=10;
	private Searcher search = null;
	public Server(){  
		super();
		search=new Searcher(new String(INDEX_ABSOLUTE_PATH + "/index"));
	}
	
	public ScoreDoc[] showList(ScoreDoc[] results, int page){
		if(results == null || results.length < (page - 1) * PAGE_RESULT){
			return null;
		}
		int start = Math.max((page - 1) * PAGE_RESULT, 0);
		int docnum = Math.min(results.length - start, PAGE_RESULT);
		ScoreDoc[] ret = new ScoreDoc[docnum];
		for(int i = 0; i < docnum; i++){   
			ret[i] = results[start + i];
		}
		return ret;
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		
		String queryString = request.getParameter("query");
		String pageString = request.getParameter("page");
		
		if(queryString == null || queryString.replace(" ", "").length() == 0) {
			response.sendRedirect("/SixerSearcher/search.jsp");
			return;
		}
		
		int page = 1;
		if(pageString!=null){
			page=Integer.parseInt(pageString);
		}
		
		TopDocs results = search.searchQuery(queryString, 100);
		String[] paths = null;
		String[] titles = null;
		String[] descriptions = null;
		
		if (results != null) {
			ScoreDoc[] hits = showList(results.scoreDocs, page);
			
			paths = new String[hits.length];
			titles = new String[hits.length];
			descriptions = new String[hits.length];
			for (int i = 0; i < hits.length && i < PAGE_RESULT; i++) {
				paths[i] = "news.tsinghua.edu.cn" + search.getDoc(hits[i].doc).get("path").replace(PAGE_ROOT_DIR, "");
				System.out.println(paths[i]);
				titles[i] = search.getDecoratedTitle(hits[i].doc) ;
				descriptions[i] = search.getDecoratedDescription(hits[i].doc);
			}  
			
		}else{
			System.out.println("result null");
		}
		ArrayList<String> a1 = search.getCompletion(queryString);
		ArrayList<String> b1 = search.getRelateRecommend(queryString);
		
		request.setAttribute("currentQuery",queryString);
		request.setAttribute("currentPage", page);
		request.setAttribute("paths", paths);
		request.setAttribute("titles", titles);
		request.setAttribute("descriptions", descriptions);
		
		String [] autocomplete = (String[])a1.toArray(new String[a1.size()]);
		String [] suggestions = (String[])b1.toArray(new String[b1.size()]);
				
		request.setAttribute("autocomplete", autocomplete);
		request.setAttribute("suggestions", suggestions);
		
		request.getRequestDispatcher("/results.jsp").forward(request,response); 
		
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		boolean ajax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
	    if (ajax) {
			String word = request.getParameter("word");
			System.out.println(word);
			
			List<String> completeList = search.getCompletion(word);
		    String json = new Gson().toJson(completeList);
		    response.setContentType("application/json");
		    response.setCharacterEncoding("UTF-8");
		    response.getWriter().write(json);
	    }
	    else {
	    	System.out.print("NOT AJAX\n");
	    }
	    //this.doGet(request, response);
	}
}

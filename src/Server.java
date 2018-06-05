

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * Servlet implementation class Server
 */
public class Server extends HttpServlet {
	private static final long serialVersionUID = 1L;
	//private YourClassName searcher; 
    /**
     * Default constructor. 
     */
    public Server() {
        // TODO Auto-generated constructor stub
    	// searcher = new YourClassName;
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		String queryString=request.getParameter("query");
		String pageString=request.getParameter("page");
		int currentPage = 1;
		int pageNum = 10;
		System.out.println("test");
		if (pageString != null && !pageString.isEmpty())
			currentPage = Integer.parseInt(pageString);
		if(queryString.length() == 0){
			System.out.println("null query.ddd");
			response.sendRedirect("/search_engine/search.jsp");
			//request.getRequestDispatcher("/Image.jsp").forward(request, response);
		}else{
			/*System.out.println(URLDecoder.decode(queryString,"utf-8"));
			System.out.println(URLDecoder.decode(queryString,"gb2312"));
			String[] tags=null;
			String[] paths=null;
			//TopDocs results=search.searchQuery(queryString, "abstract", 100);
			ScoreDoc [] results=search.searchMultiTermQuery(queryString, "abstract", 100);
			if (results != null) {
				ScoreDoc[] hits = showList(results, page);
				if (hits != null) {
					tags = new String[hits.length];
					paths = new String[hits.length];
					for (int i = 0; i < hits.length && i < PAGE_RESULT; i++) {
						Document doc = search.getDoc(hits[i].doc);
						System.out.println("doc=" + hits[i].doc + " score="
								+ hits[i].score + " picPath= "
								+ doc.get("picPath")+ " tag= "+doc.get("abstract"));
						tags[i] = doc.get("abstract");
						paths[i] = picDir + doc.get("picPath");
					}

				} else {
					System.out.println("page null");
				}
			}else{
				System.out.println("result null");
			}
			*/
			String [] paths = null;
			paths = new String[1];
			paths[0] = "news.tsinghua/test.link";
			String [] titles = null;
			titles = new String[1];
			titles[0] = queryString;
			String [] descriptions = null;
			descriptions = new String[1];
			descriptions[0] = "test.link";
			
			
			
			System.out.println(queryString+4);
			request.setAttribute("currentPage", currentPage);
			request.setAttribute("currentQuery", queryString);
			request.setAttribute("pageNum", pageNum);
			//request.setAttribute("imgTags", tags);
			
			
			request.setAttribute("paths", paths);
			request.setAttribute("titles", titles);
			request.setAttribute("descriptions", descriptions);
			request.setAttribute("suggestions", descriptions);
			
			request.getRequestDispatcher("//show.jsp").forward(request,
					response);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}

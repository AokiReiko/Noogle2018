import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PageRank {
	
	final int MAX_ITER_TIMES = 25;
	final double ALPHA = 0.15;
	
	ArrayList<PagePR> pageList = new ArrayList<PagePR>();
	HashMap<String, Integer> pageToIndex = new HashMap<String, Integer>();
	// filePathList是各个页面的path,path中包含baseDir,
	// 然后设置Page的filepath为path，pagename是path去掉baseDir，即baseDir中的相对路径
	// filePath在打开文件的时候使用，pageName用来做哈希，用于建立连接关系的时候进行查找
	public void init(ArrayList<String> filePathList, String baseDir) {
		baseDir = baseDir.replace('\\', '/');
		int indexCnt = 0;
		for (String filePath : filePathList) {
			PagePR page = new PagePR();
			pageList.add(page);
			page.filePath = filePath;
			String filePathFormatted = filePath.replace('\\', '/');
			page.pageName = filePathFormatted.replace(baseDir, "");
			pageToIndex.put(page.pageName, indexCnt++);
		}
		
		System.out.println("Load pages ...");
		// 读每一个网页
		for (PagePR page : pageList) {
			String html = "";
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(page.filePath)));
				while (true) {
					String line = reader.readLine();
					if (line == null) break;
					html += line + "\n";
				}
				reader.close();
			} catch (Exception e) {
				System.out.println("Read " + page.filePath + " error!");
			}
			// html是网页的所有内容
			// TODO:html不需要处理编码吗?
			// 然后查找其中的链接
			Pattern pattern = Pattern.compile("href=\"(.*?)\"");
			Matcher matcher = pattern.matcher(html);
			while (matcher.find()) {
				String outName = matcher.group(1);
				// 查找链接Page是否存在
				if (!pageToIndex.containsKey(outName)) continue;
				int outIndex = pageToIndex.get(outName);
				// 根据hash index找到pageList中的page,这样提高效率同时占用空间较少
				PagePR outPage = pageList.get(outIndex);
				// 使用入度出度列表建立链接关系
				page.outList.add(outPage);
				outPage.inList.add(page);
			}
		}
		System.out.println("Load pages finish !");
		
		// 计算PageRank
		System.out.println("Calculate page rank ...");
		int n = pageList.size();
		for (PagePR page : pageList) {
			page.pageRank = 1.0 / n;
		}
		for (int iterTimes = 0; iterTimes < MAX_ITER_TIMES; ++iterTimes) {
			System.out.println("Iter " + iterTimes);
			// 注意这里要重新设置
			double noOutPageRankSum = 0;
			for (PagePR page : pageList) {
				if (page.outList.size() > 0) {
					double x = (1 - ALPHA) * page.pageRank / page.outList.size();
					for (PagePR outPage : page.outList) {
						outPage.newPageRank += x;
					}
				} else {
					noOutPageRankSum += page.pageRank;
				}
			}
			for (PagePR page : pageList) {
				page.pageRank = ALPHA / n + page.newPageRank + (1 - ALPHA) * noOutPageRankSum / n;
				page.newPageRank = 0;
			}
		}
		System.out.println("Calculate page rank finish !");
	}
	
	public void print(String fileName) {
		ArrayList<PagePR> q = (ArrayList<PagePR>)pageList.clone();
		Collections.sort(q, new Comparator<PagePR>() {
			public int compare(PagePR o0, PagePR o1) {
				Double d0 = o0.pageRank;
				Double d1 = o1.pageRank;
				return d1.compareTo(d0);
			}
		});
		try {
			FileWriter fw = new FileWriter(fileName);
			for (PagePR page : q) {
				fw.write(page.pageName + " " + page.pageRank+ "\n");
			}
            fw.close();  
		} catch (IOException e) {
			for (PagePR page : q) {
				System.out.println(page.pageName + " " + page.pageRank );
			}
			e.printStackTrace();
		}  
	}
}

class PagePR {
	public String filePath;
	public String pageName;
	public ArrayList<PagePR> inList = new ArrayList<PagePR>();
	public ArrayList<PagePR> outList = new ArrayList<PagePR>();
	public double pageRank = 0;
	public double newPageRank = 0;
}

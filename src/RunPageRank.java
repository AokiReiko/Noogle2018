import java.io.File;
import java.util.ArrayList;

public class RunPageRank {
	// TODO hard code
	static final String pageRootDir = "D:\\tsinghua\\20180518172144\\mirror\\news.tsinghua.edu.cn";
	static final String pageRankFile = "rank.txt";
	
	
	public static void main(String[] args) {
		ArrayList<String> filePathList = Tools.getFilePathList(pageRootDir, "html");
		System.out.println("Get " + filePathList.size() + " files.");
		PageRank pageRank = new PageRank();
		pageRank.init(filePathList, pageRootDir);
		pageRank.print(pageRankFile);
		System.out.println("print pagerank finish!");
	}
}

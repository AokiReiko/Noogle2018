import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
public class Tools {

	// 每个文档docID作为键，值是[wordID,]
	static HashMap<Integer, ArrayList<Integer>> tf = new HashMap<Integer, ArrayList<Integer>>();
	// 其实是df，键是wordID，值是出现在多少doc中
	static HashMap<Integer, Integer> df = new HashMap<Integer, Integer>();
	static HashMap<String, Integer> s2i = new HashMap<String, Integer>();
	static HashMap<Integer, String> i2s = new HashMap<Integer, String>();
	
	static int icnt = 0;
	static ArrayList<String> getFilePathList(String path, String type) {
		File[] fileList = (new File(path)).listFiles();
		ArrayList<String> filePathList = new ArrayList<String>();
		for (File file : fileList) {
			if (file.isDirectory()) {
				ArrayList<String> sonFilePathList = getFilePathList(file.getAbsolutePath(), type);
				filePathList.addAll(sonFilePathList);
			} else if (file.isFile() && getFileType(file.getName()).toLowerCase().equals(type)) {
				filePathList.add(file.getAbsolutePath());
			}
		}
		return filePathList;
	}
	
	static String getFileType(String fileName) {
		int pos = fileName.lastIndexOf('.');
		return fileName.substring(pos + 1);
	}

	static void add(String word, int freq, int docID) {
		// 数字没有意义，首先过滤掉
		for (int i = 0; i < word.length(); ++i) {
			char c = word.charAt(i);
			if (c >= '0' && c <= '9') return;
		}
		// 每个word映射到一个整数wordID
		int wordID;
		if (s2i.containsKey(word)) {
			wordID = s2i.get(word);
		} else {
			s2i.put(word, icnt);
			i2s.put(icnt, word);
			df.put(icnt, 0);
			wordID = icnt++;
		}
//		if (!tf.containsKey(docID)) {
//			tf.put(docID, new ArrayList<Integer>());
//		}
		// 其实是df，键是wordID，值是出现在多少doc中
		// TODO:可能有问题，如果是重合的doc呢？可能传来的数据保证不会出现这个问题
		df.put(wordID, df.get(wordID) + 1);
		// 每个文档docID作为键，值是[wordIDs]
		// TODO:这里有问题，如果tf.get(docID).containsKey(wordID)的时候应该进行累加吧，
		// 可能传来的数据保证不会出现这个问题。
//		tf.get(docID).add(wordID);
	}
}

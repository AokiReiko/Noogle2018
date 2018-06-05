import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
public class Tools {

	// ÿ���ĵ�docID��Ϊ����ֵ��[wordID,]
	static HashMap<Integer, ArrayList<Integer>> tf = new HashMap<Integer, ArrayList<Integer>>();
	// ��ʵ��df������wordID��ֵ�ǳ����ڶ���doc��
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
		// ����û�����壬���ȹ��˵�
		for (int i = 0; i < word.length(); ++i) {
			char c = word.charAt(i);
			if (c >= '0' && c <= '9') return;
		}
		// ÿ��wordӳ�䵽һ������wordID
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
		// ��ʵ��df������wordID��ֵ�ǳ����ڶ���doc��
		// TODO:���������⣬������غϵ�doc�أ����ܴ��������ݱ�֤��������������
		df.put(wordID, df.get(wordID) + 1);
		// ÿ���ĵ�docID��Ϊ����ֵ��[wordIDs]
		// TODO:���������⣬���tf.get(docID).containsKey(wordID)��ʱ��Ӧ�ý����ۼӰɣ�
		// ���ܴ��������ݱ�֤�������������⡣
//		tf.get(docID).add(wordID);
	}
}

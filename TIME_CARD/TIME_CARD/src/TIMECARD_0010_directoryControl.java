import javax.swing.JFileChooser;
import javax.swing.JFrame;

//選択ダイアログフォルダ選択
//ダイアログに表示するメッセージの値を引数として渡す
//↓呼び出しサンプル
		//データ出力先の選択
		//String ans_txt="";
		//String selected = "";
		//TIMECARD_0010_directoryControl call01;
		//call01=new TIMECARD_0010_directoryControl();
		//selected = call01.file_choice("出力先選択");
		//ans_txt = ans_txt + selected + "\n";


public class TIMECARD_0010_directoryControl extends JFrame{
	static String MSG;
	static String selected;
	public static String file_choice(String f1){
		MSG = f1;
		final TIMECARD_0010_directoryControl frame = new TIMECARD_0010_directoryControl();

				int result = frame.fileChooser.showDialog(frame, "決定");
				if (result != JFileChooser.APPROVE_OPTION){

				}
				selected = frame.fileChooser.getSelectedFile().getAbsolutePath();
				return selected;

	}

	private JFileChooser fileChooser;
	public TIMECARD_0010_directoryControl(){
		this.fileChooser = new JFileChooser("/");
		this.fileChooser.setDialogTitle(MSG);
		this.fileChooser.setFileSelectionMode(
				JFileChooser.DIRECTORIES_ONLY);
	}
}
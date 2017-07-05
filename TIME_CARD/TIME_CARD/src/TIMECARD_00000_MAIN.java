import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class TIMECARD_00000_MAIN{

	static String uid[];
	static String uname[];
	static String uid_uname[];
	//static String fp = "C:/MIYATA_TRAFFIC/TIME_CARD/";	//ファイル格納フォルダ
	static String fp = "";	//ファイル格納フォルダ　アプリケーションと同じ場所
	static String fp_sound="SOUND/";	//音源の場所
	static String ini_name = "ini.txt";
	static String output_name = "";
	static int u_id_count;
	static Timestamp LastIn[];
	static Timestamp LastOUT[];
	static String search_txt;//実績検索結果

	public static void main (String[] args) {
		time_card();
	}
	public static void time_card(){
		//出退勤ボタンを押すとタイムスタンプを取得してテキストデータに書き込みます

		//出退勤入力フォームの作成
		final JFrame wait_fm = new JFrame();
		//ウィンドウタイトルの設定
		wait_fm.setTitle("少々お待ちください");
		//表示位置サイズの設定（表示横位置,表示縦位置,横幅,縦幅）
		wait_fm.setBounds(200, 200, 200, 200);
		//レイアウト無効
		wait_fm.setLayout(null);
		//ウィンドウの閉じるボタンでプログラム終了しない
		//閉じるボタンでDBのコネクション閉じてから終了させたい為
		wait_fm.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		wait_fm.setVisible(true);

		u_id_count=0;	//ユーザーIDのカウント数

		String s = null;
		String s2 = null;
		String s3 = null;
		int counter01 = 0;
		int counter02 = 0;
		String cs = "";

		try {
			BufferedReader in01 =new BufferedReader(
					new InputStreamReader(
							new FileInputStream(fp+ini_name),"Shift-JIS"));

			while((s=in01.readLine()) !=null){
				counter01 = counter01+1;
				String ws = "";
				if(s.length()>13){
					ws = s.substring(0,4);
					if(ws.equals("u_id")){
						u_id_count = u_id_count + 1;
					}
					if(s.substring(0,6).equals("output")){
						output_name = s.substring(13,s.length());
					}
				}
			}
			in01.close();

			uid = new String[u_id_count];
			uname = new String[u_id_count];
			uid_uname = new String[u_id_count];
			LastIn = new Timestamp[u_id_count];
			LastOUT = new Timestamp[u_id_count];

			BufferedReader in02 =new BufferedReader(
					new InputStreamReader(
						new FileInputStream(fp+ini_name),"Shift-JIS"));

			counter01 = 0;
			while((s2=in02.readLine()) !=null){
				String ws = "";
				if(s2.length()>13){
					ws = s2.substring(0,4);
					if(ws.equals("u_id")){
						uid[counter01] = s2.substring(4,8);
						uname[counter01] = s2.substring(13,s2.length());
						uid_uname[counter01] = "("+uid[counter01]+")"+uname[counter01];
						counter01 = counter01+1;
					}
				}
			}
			in02.close();

			BufferedReader in03 =new BufferedReader(
					new InputStreamReader(
						new FileInputStream(fp+output_name),"Shift-JIS"));

			for(int i = 0;i<u_id_count;i++){

				try {
					LastIn[i]=new Timestamp(new SimpleDateFormat("yyyy'/'MM'/'dd' 'HH':'mm':'ss").parse("1900/01/01 00:00:00").getTime());
					LastOUT[i]=new Timestamp(new SimpleDateFormat("yyyy'/'MM'/'dd' 'HH':'mm':'ss").parse("1900/01/01 00:00:00").getTime());
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}
			StringTokenizer token;
			counter01 = 0;

			//現在の日付時刻の取得
			Calendar cal= Calendar.getInstance();
			Timestamp ps=new Timestamp(cal.getTimeInMillis());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy'/'MM'/'dd");
			String dateString = sdf.format(ps);


			while((s3=in03.readLine()) !=null){
				String[] t = s3.split(",", 0);
				if(t.length>=5){
					if(t[4].length()>10){
						if(dateString.equals((t[4]).substring(0, 10))){
							for(int i = 0;i<u_id_count;i++){
								try {
									if(uid[i].equals(t[0])){
										Timestamp WT = new Timestamp(new SimpleDateFormat("yyyy'/'MM'/'dd' 'HH':'mm':'ss").parse(t[4]).getTime());
										if("1".equals(t[2])){
											if(WT.after(LastIn[i])){
												LastIn[i] = WT;
												i = u_id_count;
												//System.out.println("HIT今日："+dateString+ t[4].substring(0, 10));
											}
										}
										if("2".equals(t[2])){
											if(WT.after(LastOUT[i])){
												LastOUT[i] = WT;
												i = u_id_count;
												//System.out.println("HIT今日："+dateString+ t[4].substring(0, 10));
											}
										}
									}
								} catch (ParseException e1) {
									e1.printStackTrace();
								}
							}
						}
					}
				}
			}
			in03.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
  		} catch (IOException e) {
			e.printStackTrace();
		}

		wait_fm.setVisible(false);

		//出退勤入力フォームの作成
		final JFrame login_fm = new JFrame();
		//ウィンドウタイトルの設定
		login_fm.setTitle("出退勤");
		//表示位置サイズの設定（表示横位置,表示縦位置,横幅,縦幅）
		login_fm.setBounds(200, 200, 400, 350);
		//レイアウト無効
		login_fm.setLayout(null);
		//ウィンドウの閉じるボタンでプログラム終了しない
		//閉じるボタンでDBのコネクション閉じてから終了させたい為
		login_fm.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		//ユーザーID入力BOX
		final JTextField ID_TB = new JTextField("");
		ID_TB.setBounds(40, 10, 200, 20);

		//出退勤フラグ入力BOX
		final JTextField FLG_TB = new JTextField("");
		FLG_TB.setBounds(280, 10, 20, 20);

		//EXITボタン
		JButton login_exit_btn=new JButton();
		login_exit_btn.setText("EXIT");
		login_exit_btn.setBounds(260, 280, 100, 20);

		//ユーザー追加ボタン
		JButton add_user_btn=new JButton();
		add_user_btn.setText("add_user");
		add_user_btn.setBounds(20, 280, 100, 20);

		//データ抽出ボタン
		JButton exp_btn=new JButton();
		exp_btn.setText("データ抽出");
		exp_btn.setBounds(260, 250, 100, 20);

		//出勤ボタン
		JButton IN_btn=new JButton();
		IN_btn.setText("出勤");
		IN_btn.setBounds(40, 80, 100, 100);

		//退勤ボタン
		JButton OUT_btn=new JButton();
		OUT_btn.setText("退勤");
		OUT_btn.setBounds(170, 80, 100, 100);

		//選択フォーム
		final JComboBox wh_cb = new JComboBox(uid_uname);
		wh_cb.setBounds(40, 40, 200, 20);

		login_fm.add(ID_TB);
		login_fm.add(FLG_TB);
		login_fm.add(IN_btn);
		login_fm.add(OUT_btn);
		login_fm.add(wh_cb);
		login_fm.add(exp_btn);
		login_fm.add(add_user_btn);
		login_fm.add(login_exit_btn);
		login_fm.setVisible(true);

		//ユーザーID入力画面でエンターlキーを押したときの挙動
		ID_TB.addActionListener(new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent e){
				String ST = "";
				ST = ID_TB.getText();
				int checker = 0;
				int counter = 0;
				//System.out.println(uid[counter]);
				while(checker == 0){
					if(u_id_count==counter){
						checker = 1;
					}else{
						if(uid[counter].equals(ST)){
							checker = 1;
							wh_cb.setSelectedIndex(counter);
							FLG_TB.setText("");
							FLG_TB.requestFocusInWindow();
						}
						counter = counter+1;
					}
				}
			}
		});

		//フラグ入力画面でエンターlキーを押したときの挙動
		FLG_TB.addActionListener(new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent e){

				int slected = 0;
				int flg = 0;

				String ST = "";
				ST = ID_TB.getText();
				int checker = 0;
				int counter = 0;

				flg = Integer.parseInt(FLG_TB.getText());
				slected = wh_cb.getSelectedIndex();

				//選択ユーザーのインデックス番号と退勤フラグをデータ書き出しサブプログラムに引き渡し
				IN_OUT(slected,flg);

				ID_TB.setText("");
				FLG_TB.setText("");
				ID_TB.requestFocusInWindow();
				wh_cb.setSelectedIndex(0);
			}
		});

		//出勤ボタン押下時の挙動
		IN_btn.addActionListener(new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent e){

				int slected = 0;
				int flg = 1;
				slected = wh_cb.getSelectedIndex();

				//選択ユーザーのインデックス番号と退勤フラグをデータ書き出しサブプログラムに引き渡し
				IN_OUT(slected,flg);

				ID_TB.setText("");
				FLG_TB.setText("");
				ID_TB.requestFocusInWindow();
				wh_cb.setSelectedIndex(0);
			}
		});

		//退勤ボタン押下時の挙動
		OUT_btn.addActionListener(new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent e){

				int slected = 0;
				int flg = 2;
				slected = wh_cb.getSelectedIndex();

				//選択ユーザーのインデックス番号と退勤フラグをデータ書き出しサブプログラムに引き渡し
				IN_OUT(slected,flg);

				ID_TB.setText("");
				FLG_TB.setText("");
				ID_TB.requestFocusInWindow();
				wh_cb.setSelectedIndex(0);
			}
		});
		//ユーザー追加ボタン押下時の挙動
		add_user_btn.addActionListener(new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent e){
				login_fm.setVisible(false);
				//ユーザー追加フォームの作成
				final JFrame user_add_fm = new JFrame();
				//ウィンドウタイトルの設定
				user_add_fm.setTitle("ユーザー追加");
				//表示位置サイズの設定（表示横位置,表示縦位置,横幅,縦幅）
				user_add_fm.setBounds(200, 200, 400, 400);
				//レイアウト無効
				user_add_fm.setLayout(null);
				//ウィンドウの閉じるボタンでプログラム終了しない
				//閉じるボタンでDBのコネクション閉じてから終了させたい為
				user_add_fm.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

				JLabel uid_LB = new JLabel("ID(数字4桁)：");
				uid_LB.setBounds(10, 40, 90, 20);

				JLabel uname_LB = new JLabel("名前：");
				uname_LB.setBounds(10, 70, 90, 20);

				DecimalFormat df = new DecimalFormat("####");
				// 整数値の最小値
		        df.setMinimumIntegerDigits(4);
		        // 整数値の最大値
		        df.setMaximumIntegerDigits(4);
		        final JFormattedTextField uid_TB = new JFormattedTextField(df);
				uid_TB.setBounds(100, 40, 50, 20);

				final JTextField uname_TB = new JTextField("");
				uname_TB.setBounds(100, 70, 100, 20);

				//ENTRYボタン
				JButton entry_btn=new JButton();
				entry_btn.setText("ENTRY");
				entry_btn.setBounds(260, 300, 100, 20);

				//EXITボタン
				JButton exit_btn=new JButton();
				exit_btn.setText("EXIT");
				exit_btn.setBounds(260, 330, 100, 20);

				user_add_fm.add(uname_LB);
				user_add_fm.add(uname_TB);
				user_add_fm.add(uid_LB);
				user_add_fm.add(uid_TB);
				user_add_fm.add(entry_btn);
				user_add_fm.add(exit_btn);
				user_add_fm.setVisible(true);

				//entryボタン押下時の挙動
				entry_btn.addActionListener(new AbstractAction(){
					@Override
					public void actionPerformed(ActionEvent e){
						String w_uid = "";
						String w_uname = "";
						w_uid = uid_TB.getText();
						w_uname = uname_TB.getText();

						//null空白チェック
						if(w_uid == null || w_uid.equals("")){
							JOptionPane.showMessageDialog(null, "IDは必須です", "警告", 0);
						}else{
							if(w_uname == null || w_uname.equals("")){
								JOptionPane.showMessageDialog(null, "名前は必須です", "警告", 0);
							}
							else{
								//ID重複チェック
								int checker01 = 0;
								int checker02 = 0;
								int counter = 0;
								while(checker01 == 0){
									if(u_id_count==counter){
										checker01 = 1;
									}else{
										if(uid[counter].equals(w_uid)){
											checker01 = 1;
											checker02 = 1;
										}
										counter = counter+1;
									}
								}
								if(checker02 == 1){
									JOptionPane.showMessageDialog(null, "IDが重複しています", "警告", 0);
								}
								else{
									//System.out.println(fp+ini_name);
									try {
										BufferedWriter out01 = new BufferedWriter(
												new OutputStreamWriter(
														new FileOutputStream(fp+ini_name,true),"Shift-JIS"));
										String output_txt01 = "u_id"+w_uid + "          ";
										output_txt01 = output_txt01.substring(0,10) + " = " + w_uname;
										out01.write(output_txt01);
										out01.newLine();
										out01.close();
										uid_TB.setText("");
										uname_TB.setText("");
										uid_TB.requestFocusInWindow();
										JOptionPane.showMessageDialog(null, "("+w_uid+")"+w_uname+"登録しました", "INFO", 1);
										//System.out.println(output_txt01);
									} catch (IOException e2) {
										JOptionPane.showMessageDialog(null, "ユーザー登録に失敗しました", "警告", 0);
										e2.printStackTrace();
									}
								}
							}
						}
					}
				});

				//EXITボタン押下時の挙動
				exit_btn.addActionListener(new AbstractAction(){
					@Override
					public void actionPerformed(ActionEvent e){
						user_add_fm.setVisible(false);
						time_card();
					}
				});
			}
		});

		//データ抽出ボタン押下時の挙動
		exp_btn.addActionListener(new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent e){

				//現在の日付時刻の取得
				Calendar cal= Calendar.getInstance();
				Timestamp ps=new Timestamp(cal.getTimeInMillis());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy'/'MM'/'dd");
				String dateString = sdf.format(ps);

				login_fm.setVisible(false);

				//出退勤入力フォームの作成
				final JFrame exp_fm = new JFrame();
				//ウィンドウタイトルの設定
				exp_fm.setTitle("データ抽出");
				//表示位置サイズの設定（表示横位置,表示縦位置,横幅,縦幅）
				exp_fm.setBounds(200, 200, 400, 350);
				//レイアウト無効
				exp_fm.setLayout(null);
				//ウィンドウの閉じるボタンでプログラム終了しない
				//閉じるボタンでDBのコネクション閉じてから終了させたい為
				exp_fm.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

				//ユーザーID入力BOX
				final JTextField ID_TB = new JTextField("");
				ID_TB.setBounds(40, 10, 200, 20);

				//選択フォーム
				final JComboBox wh_cb = new JComboBox(uid_uname);
				wh_cb.setBounds(40, 40, 200, 20);

				//日付範囲指定入力ボックス
				JLabel day_com = new JLabel("抽出対象範囲");
				day_com.setBounds(40, 70, 200, 20);

				JLabel day_com2 = new JLabel("～");
				day_com2.setBounds(130, 100, 20, 20);

		        //日付開始
		        final JFormattedTextField str_TB = new JFormattedTextField(sdf);
		        str_TB.setValue(ps);
				str_TB.setBounds(40, 100, 80, 20);

				//日付終了
		        final JFormattedTextField end_TB = new JFormattedTextField(sdf);
		        end_TB.setValue(ps);
				end_TB.setBounds(160, 100, 80, 20);


				//全ユーザー抽出ボタン
				JButton output_ALL_btn=new JButton();
				output_ALL_btn.setText("全員抽出");
				output_ALL_btn.setBounds(260, 220, 100, 20);

				//抽出ボタン
				JButton output_btn=new JButton();
				output_btn.setText("抽出");
				output_btn.setBounds(260, 250, 100, 20);

				//EXITボタン
				JButton exit_btn=new JButton();
				exit_btn.setText("EXIT");
				exit_btn.setBounds(260, 280, 100, 20);

				exp_fm.add(str_TB);
				exp_fm.add(end_TB);
				exp_fm.add(day_com);
				exp_fm.add(day_com2);
				exp_fm.add(ID_TB);
				exp_fm.add(wh_cb);
				exp_fm.add(output_ALL_btn);
				exp_fm.add(output_btn);
				exp_fm.add(exit_btn);
				exp_fm.setVisible(true);

				//ユーザーID入力画面でエンターlキーを押したときの挙動
				ID_TB.addActionListener(new AbstractAction(){
					@Override
					public void actionPerformed(ActionEvent e){
						String ST = "";
						ST = ID_TB.getText();
						int checker = 0;
						int counter = 0;
						//System.out.println(uid[counter]);
						while(checker == 0){
							if(u_id_count==counter){
								checker = 1;
							}else{
								if(uid[counter].equals(ST)){
									checker = 1;
									wh_cb.setSelectedIndex(counter);
								}
								counter = counter+1;
							}
						}
					}
				});
				//全員抽出ボタン押下時の挙動
				output_ALL_btn.addActionListener(new AbstractAction(){
					@Override
					public void actionPerformed(ActionEvent e){

						//現在の日付時刻の取得して日付範囲初期値
						Calendar cal= Calendar.getInstance();
						Timestamp ps=new Timestamp(cal.getTimeInMillis());
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddmmhhss");
						String dateString = sdf.format(ps);

						String ans_txt="";
						String selected = "";
						TIMECARD_0010_directoryControl call01;
						call01=new TIMECARD_0010_directoryControl();
						selected = call01.file_choice("出力先選択");
						ans_txt = ans_txt + selected + "\\"+dateString+"出勤退勤抽出"+".csv";
						String o_txt = "";

						o_txt = "CD,NAME,日付,出勤,退勤";
						try {
							BufferedWriter out01 = new BufferedWriter(
									new OutputStreamWriter(
											new FileOutputStream(ans_txt,true),"Shift-JIS"));
							String output_txt01 = "";
							output_txt01 = o_txt;
							out01.write(output_txt01);
							out01.newLine();
							out01.close();

						} catch (IOException e2) {
							e2.printStackTrace();
						}

						int slected = wh_cb.getSelectedIndex();
						String str_day = str_TB.getText();
						String end_day = end_TB.getText();
						for(int i=0;i<u_id_count;i++){
							slected = i;
							search(slected,str_day,end_day,ans_txt);
						}
						JOptionPane.showMessageDialog(null, ans_txt + "　にデータ書き出しました", "INFO", 1);
						try {
							File file = new File(ans_txt);
				            Desktop desktop = Desktop.getDesktop();
								desktop.open(file);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						//System.out.println(search_txt);

						//exp_fm.setVisible(false);
						//time_card();
					}
				});


				//抽出ボタン押下時の挙動
				output_btn.addActionListener(new AbstractAction(){
					@Override
					public void actionPerformed(ActionEvent e){

						//現在の日付時刻の取得して日付範囲初期値
						Calendar cal= Calendar.getInstance();
						Timestamp ps=new Timestamp(cal.getTimeInMillis());
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddmmhhss");
						String dateString = sdf.format(ps);

						String ans_txt="";
						String selected = "";
						TIMECARD_0010_directoryControl call01;
						call01=new TIMECARD_0010_directoryControl();
						selected = call01.file_choice("出力先選択");
						ans_txt = ans_txt + selected + "\\"+dateString+"出勤退勤抽出"+".csv";

						String o_txt = "";

						o_txt = "CD,NAME,日付,出勤,退勤";
						try {
							BufferedWriter out01 = new BufferedWriter(
									new OutputStreamWriter(
											new FileOutputStream(ans_txt,true),"Shift-JIS"));
							String output_txt01 = "";
							output_txt01 = o_txt;
							out01.write(output_txt01);
							out01.newLine();
							out01.close();

						} catch (IOException e2) {
							e2.printStackTrace();
						}


						int slected = wh_cb.getSelectedIndex();
						String str_day = str_TB.getText();
						String end_day = end_TB.getText();

						search(slected,str_day,end_day,ans_txt);
						JOptionPane.showMessageDialog(null, ans_txt + "　にデータ書き出しました", "INFO", 1);
			            try {
							File file = new File(ans_txt);
				            Desktop desktop = Desktop.getDesktop();
								desktop.open(file);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						//System.out.println(search_txt);

						//exp_fm.setVisible(false);
						//time_card();
					}
				});

				//EXITボタン押下時の挙動
				exit_btn.addActionListener(new AbstractAction(){
					@Override
					public void actionPerformed(ActionEvent e){
						exp_fm.setVisible(false);
						login_fm.setVisible(true);
					}
				});
			}
		});

		//EXITボタン押下時の挙動
		login_exit_btn.addActionListener(new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent e){
				System.exit(0);
			}
		});
	}

	public static void IN_OUT(int f0,int f1){
		//選択ユーザーのインデックス番号　f0
		//出勤退勤フラグ　f1
		//受け取って、現在時刻で出勤退勤のテキストデータ書き出し

		//現在の日付時刻の取得
		Calendar cal= Calendar.getInstance();
		Timestamp ps=new Timestamp(cal.getTimeInMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy'/'MM'/'dd' 'HH':'mm':'ss");
		String dateString = sdf.format(ps);

		int FLG = f1;
		int slected = f0;
		String output_uid = "";
		String output_uname = "";

		output_uid = uid[slected];
		output_uname = uname[slected];


		//FLG=1　出勤
		if(FLG == 1){
			String LASTINST = sdf.format(LastIn[slected]);
			LastIn[slected] = ps;
			if(LASTINST.substring(0,10).equals(dateString.substring(0,10))){
				JOptionPane.showMessageDialog(null, "既に出勤しています", "警告", 0);
			}
			else{
				try {
					BufferedWriter out01 = new BufferedWriter(
							new OutputStreamWriter(
									new FileOutputStream(fp+output_name,true),"Shift-JIS"));
					String output_txt01 = "";
					output_txt01 = uid[slected]+","+uname[slected]+",1,出勤,"+dateString+","+dateString;
					out01.write(output_txt01);
					out01.newLine();
					out01.close();
					//お出迎え音
					sound(fp_sound+"IN_SOUND.wav",1);

					//お出迎えメッセージ
					JOptionPane.showMessageDialog(null, uname[slected]+"\n"+"WELCOME TO THE WORLD!!", "INFO", 1);

				} catch (IOException e2) {
					JOptionPane.showMessageDialog(null, "出勤時刻の書き出しに失敗しました"+"\n"+"書き込みファイル使用中ではありませんか?", "警告", 0);
					e2.printStackTrace();
				}
			}
		}
		//FLG=2　退勤
		if(FLG == 2){
			String LASTOUTST = sdf.format(LastOUT[slected]);
			LastOUT[slected] = ps;
			if(LASTOUTST.substring(0,10).equals(dateString.substring(0,10))){
				JOptionPane.showMessageDialog(null, "既に退勤しています", "警告", 0);
			}
			else{
				try {
					BufferedWriter out01 = new BufferedWriter(
							new OutputStreamWriter(
									new FileOutputStream(fp+output_name,true),"Shift-JIS"));
					String output_txt01 = "";
					output_txt01 = uid[slected]+","+uname[slected]+",2,退勤,"+dateString+","+dateString;
					out01.write(output_txt01);
					out01.newLine();
					out01.close();
					//お出迎え音
					sound(fp_sound+"OUT_SOUND.wav",1);

					//お出迎えメッセージ
					JOptionPane.showMessageDialog(null, uname[slected]+"\n"+"WATCH OUT!! TAKE CARE BYE (-_-)/~~~", "INFO", 1);

				} catch (IOException e2) {
					JOptionPane.showMessageDialog(null, "退勤時刻の書き出しに失敗しました"+"\n"+"書き込みファイル使用中ではありませんか?", "警告", 0);
					e2.printStackTrace();
				}
			}
		}
		//FLG=9　強制入力モード
		if(FLG == 9){
			Forced_input();
		}
	}

	public static void Forced_input(){

		//現在の日付時刻の取得
		Calendar cal= Calendar.getInstance();
		Timestamp ps=new Timestamp(cal.getTimeInMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy'/'MM'/'dd' 'HH':'mm':'ss");
		String dateString = sdf.format(ps);

		//強制登録フォームの作成
		final JFrame ff_fm= new JFrame();
		//ウィンドウタイトルの設定
		ff_fm.setTitle("強制登録");
		//表示位置サイズの設定（表示横位置,表示縦位置,横幅,縦幅）
		ff_fm.setBounds(200, 200, 400, 400);
		//レイアウト無効
		ff_fm.setLayout(null);
		//ウィンドウの閉じるボタンでプログラム終了しない
		//閉じるボタンでDBのコネクション閉じてから終了させたい為
		ff_fm.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		//ユーザー選択フォーム
		final JComboBox wh_cb = new JComboBox(uid_uname);
		wh_cb.setBounds(40, 40, 200, 20);

		String[] fg_s = new String[2];
		fg_s[0] = "1:出勤";
		fg_s[1] = "2:退勤";

		final JComboBox fg_cb = new JComboBox(fg_s);
		fg_cb.setBounds(40, 70, 70, 20);

        //登録日時
        final JFormattedTextField ent_TB = new JFormattedTextField(sdf);
        ent_TB.setValue(ps);
		ent_TB.setBounds(40, 100, 180, 20);

		//ENTRYボタン
		JButton entry_btn=new JButton();
		entry_btn.setText("ENTRY");
		entry_btn.setBounds(260, 300, 100, 20);

		//EXITボタン
		JButton exit_btn=new JButton();
		exit_btn.setText("EXIT");
		exit_btn.setBounds(260, 330, 100, 20);

		ff_fm.add(wh_cb);
		ff_fm.add(fg_cb);
		ff_fm.add(ent_TB);
		ff_fm.add(entry_btn);
		ff_fm.add(exit_btn);
		ff_fm.setVisible(true);

		//ENTRYボタン押下時の挙動
		entry_btn.addActionListener(new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent e){

				//現在の日付時刻の取得
				Calendar cal= Calendar.getInstance();
				Timestamp ps=new Timestamp(cal.getTimeInMillis());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy'/'MM'/'dd' 'HH':'mm':'ss");
				String dateString = sdf.format(ps);

				int slected = wh_cb.getSelectedIndex();
				String output_txt01 = "";
				int fg = fg_cb.getSelectedIndex();
				String ent_dtm = ent_TB.getText();


				if(fg == 0){
					output_txt01 = uid[slected]+","+uname[slected]+",1,出勤,"+ent_dtm+","+dateString;
				}
				if(fg == 1){
					output_txt01 = uid[slected]+","+uname[slected]+",2,退勤,"+ent_dtm+","+dateString;
				}

				try {
					BufferedWriter out01 = new BufferedWriter(
							new OutputStreamWriter(
									new FileOutputStream(fp+output_name,true),"Shift-JIS"));
					out01.write(output_txt01);
					out01.newLine();
					out01.close();
					//登録アナウンス
					if(fg == 0){
						try {
							Timestamp WT = new Timestamp(new SimpleDateFormat("yyyy'/'MM'/'dd' 'HH':'mm':'ss").parse(ent_dtm).getTime());
							if(WT.after(LastIn[slected])){
								LastIn[slected] = WT;
							}
						} catch (ParseException e1) {
							e1.printStackTrace();
						}


						JOptionPane.showMessageDialog(null, uname[slected]+"\n"+ent_dtm + "に出勤を強制登録しました", "INFO", 1);
					}
					if(fg == 1){
						try {
							Timestamp WT = new Timestamp(new SimpleDateFormat("yyyy'/'MM'/'dd' 'HH':'mm':'ss").parse(ent_dtm).getTime());
							if(WT.after(LastOUT[slected])){
								LastOUT[slected] = WT;
							}
						} catch (ParseException e1) {
							e1.printStackTrace();
						}
						JOptionPane.showMessageDialog(null, uname[slected]+"\n"+ent_dtm + "に退勤を強制登録しました", "INFO", 1);
					}

				} catch (IOException e2) {
					JOptionPane.showMessageDialog(null, "データの書き出しに失敗しました"+"\n"+"書き込みファイル使用中ではありませんか?", "警告", 0);
					e2.printStackTrace();
				}
				//fg_cb.setSelectedIndex(0);
				//wh_cb.setSelectedIndex(0);
				//ent_TB.setValue(ps);
			}
		});


		//EXITボタン押下時の挙動
		exit_btn.addActionListener(new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent e){
				ff_fm.setVisible(false);
			}
		});

	}

	public static void sound(String f0,int f1){
		//f0:鳴らす音のファイルパス
		//f1:鳴らす時間(秒)
		Clip clip = null;
	    int count = 0;      // (count+1)回 再生する
	    AudioInputStream audioInputStream;
		try
	        {   File soundFile = new File(f0);
	            audioInputStream = AudioSystem.getAudioInputStream(soundFile);
	            AudioFormat audioFormat = audioInputStream.getFormat();
	            DataLine.Info info = new DataLine.Info(Clip.class, audioFormat);
	            clip = (Clip)AudioSystem.getLine(info);
	            clip.open(audioInputStream);
	            clip.loop(count);
	        }
	        catch (UnsupportedAudioFileException e)
	        {   e.printStackTrace();  }
	        catch (IOException e)
	        {   e.printStackTrace();  }
	        catch (LineUnavailableException e)
	        {   e.printStackTrace();  }

	        // f1秒経過したら終了する
	        try
	        {   Thread.sleep(f1*1000);  }
	        catch (InterruptedException e)
	        {  }
	        clip.stop();
	}

	public static void search(int f0,String f1,String f2,String f3){
		//ユーザー選択ボックスの選択結果番号f0　開始f1終了f2日を引き渡すと
		//対象のデータを抽出
		//全ユーザーを抽出対象にするときは選択番号0～ユーザー数マイナス1までをf0に引き渡し

		String rt_txt = "";
		//現在の日付時刻の取得して日付範囲初期値
		Calendar cal= Calendar.getInstance();
		Timestamp ps=new Timestamp(cal.getTimeInMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String dateString = sdf.format(ps);

		int slected = f0;
		String output_uid = "";
		String output_uname = "";
		String ans_txt = f3;
		output_uid = uid[slected];
		output_uname = uname[slected];



		String str_day = f1;
		String end_day =f2;

		Timestamp sd;
		Timestamp ed;
		try {
			//テキストの日付をタイムスタンプにする
			sd = new Timestamp(new SimpleDateFormat("yyyy/MM/dd").parse(str_day).getTime());
			ed = new Timestamp(new SimpleDateFormat("yyyy/MM/dd").parse(end_day).getTime());
			int dcount = 0;

			//開始終了の日数を調べる
		    long dateTimeTo = ed.getTime();
		    long dateTimeFrom = sd.getTime();
		    long dayDiff = ( dateTimeTo - dateTimeFrom  ) / (1000 * 60 * 60 * 24 );
		    int DDiff = (int) (dayDiff+1);

		    //アウトプットデータ格納用
		    String target_day[] = new String[DDiff];
		    String IN[] = new String[DDiff];
		    String OUT[] = new String[DDiff];
		    String td = "";
		    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd");

		    //対象の日付をセット
		    Timestamp wts=new Timestamp(sd.getTime()-(1000 * 60 * 60 * 24 ));

		    for(int i = 0;i<DDiff;i++){
		    	Timestamp ts=new Timestamp(wts.getTime() +(1000 * 60 * 60 * 24 ));
		    	wts = ts;
		    	td = sdf2.format(ts);
		    	target_day[i] = td;
		    	IN[i] = "";
		    	OUT[i] = "";
		    }

		  //対象日付対象ユーザーの出勤と退勤を探す
		    try {
				BufferedReader in01 =new BufferedReader(
						new InputStreamReader(
								new FileInputStream(fp+output_name),"Shift-JIS"));

				StringTokenizer token;
				int counter01 = 0;
				int counter02 = 0;
				String s = "";
				while((s=in01.readLine()) !=null){
						token = new StringTokenizer(s, ",");
						String clm01 = "";
						String clm02 = "";
						String clm03 = "";
						String clm04 = "";
						String clm05 = "";
						String clm06 = "";
						String wclm05 = "";
						counter02 = 0;
						while (token.hasMoreTokens()) {
							counter02 = counter02 + 1;
							//第1カラム ユーザーID
							if(counter02==1){
								clm01 = token.nextToken();

							}
							//第2カラム ユーザー名
							if(counter02==2){
								clm02 = token.nextToken();

							}
							//第3カラム　出退勤フラグ
							if(counter02==3){
								clm03 = token.nextToken();

							}
							//第4カラム　出退勤名
							if(counter02==4){
								clm04 = token.nextToken();
							}
							//第5カラム　出退日時
							if(counter02==5){
								clm05 = token.nextToken();
								if(clm05.length()>=10){
									wclm05 = clm05.substring(0, 10);
								}
							}
							//第6カラム　登録日時
							if(counter02==6){
								clm06 = token.nextToken();
							}
						}

					int checker02 = 0;
					int counter03 = 0;
					while(checker02==0){
						if(target_day[counter03].equals(wclm05)){
							if(output_uid.equals(clm01)){
								if("1".equals(clm03)){
									checker02 = 1;
									IN[counter03] = clm05.substring(11,16);
								}
								if("2".equals(clm03)){
									checker02 = 1;
									OUT[counter03] = clm05.substring(11,16);
								}
							}
						}
						counter03=counter03+1;
						if(counter03==DDiff){
							checker02 = 1;
						}
					}
				}
				in01.close();

				for(int i = 0;i<DDiff;i++){
					rt_txt =  output_uid + ","
							+ output_uname + ","
							+ target_day[i] + ","
							+ IN[i] + ","
							+ OUT[i];
					try {
						BufferedWriter out01 = new BufferedWriter(
								new OutputStreamWriter(
										new FileOutputStream(ans_txt,true),"Shift-JIS"));
						String output_txt01 = "";
						output_txt01 = rt_txt;
						out01.write(output_txt01);
						out01.newLine();
						out01.close();
					} catch (IOException e2) {
						//JOptionPane.showMessageDialog(null, "検索結果の書き出しに失敗しました", "警告", 0);
						e2.printStackTrace();
					}
				}

			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (ParseException e1) {
			e1.printStackTrace();
		};
	}

}
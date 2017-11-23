package com.pair;

import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import com.graphviz.GraphViz;

public class MainClass {
	private static int circulate = 0;// 随机游走停止控制
	private static String OriginPath = null;// txt 的目录
	private static Hashtable<String, Graph_vertex> g = null;// 图结构
	private static Short_path spath = null;// 存储最短路径的多条路径
	private static String StartWord = null;// 随机游走起始单词
	private static Hashtable<String, Graph_vertex> path = null;// 随机游走所需的图结构，不能与原图公用
	private static Set<String> multiShortPath = new HashSet<String>();// 最短路径只输一个单词，存储未到达过的点

	public static void main(String[] args) {
		JFrame mainFrame = new JFrame("欢迎使用");

		JButton create = new JButton("生成有向图");
		JButton show = new JButton("展示图片");
		JButton query = new JButton("查询桥接词");
		JButton generate = new JButton("生成新文本");
		JButton calc = new JButton("最短路径");
		JButton random = new JButton("随即游走");

		show.setEnabled(false);
		query.setEnabled(false);
		generate.setEnabled(false);
		calc.setEnabled(false);
		random.setEnabled(false);

		mainFrame.setLayout(null);
		create.setBounds(30, 60, 120, 40);
		create.setFont(new java.awt.Font("楷书", 1, 15));
		show.setBounds(180, 60, 120, 40);
		show.setFont(new java.awt.Font("楷书", 1, 15));
		query.setBounds(330, 60, 120, 40);
		query.setFont(new java.awt.Font("楷书", 1, 15));
		generate.setBounds(30, 160, 120, 40);
		generate.setFont(new java.awt.Font("楷书", 1, 15));
		calc.setBounds(180, 160, 120, 40);
		calc.setFont(new java.awt.Font("楷书", 1, 15));
		random.setBounds(330, 160, 120, 40);
		random.setFont(new java.awt.Font("楷书", 1, 15));

		create.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// System.out.println(OpenFile());
				String tmpString = OpenFile();
				if (tmpString != null) {
					OriginPath = tmpString;
					g = createDirectedGraph(OriginPath);
					show.setEnabled(true);
					query.setEnabled(true);
					generate.setEnabled(true);
					calc.setEnabled(true);
					random.setEnabled(true);
					showDirectedGraph(g);
				}
			}
		});
		show.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String runPath = null;
				File f = new File("");
				try {
					runPath = f.getCanonicalPath();
				} catch (IOException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
				show.setEnabled(false);
				JFrame WindowPic = WindowShowPicture(runPath + "\\graph.jpg");
				WindowPic.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						show.setEnabled(true);
					}
				});
			}
		});
		query.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				query.setEnabled(false);
				JFrame windowQuery = WindowShowQueryBridge();
				windowQuery.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						query.setEnabled(true);
					}
				});
			}
		});
		generate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				generate.setEnabled(false);
				JFrame windowGenerate = WindowShowNewText();
				windowGenerate.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						generate.setEnabled(true);
					}
				});
			}
		});
		calc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				calc.setEnabled(false);
				JFrame windowCalc = WindowShowShortPath();
				windowCalc.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						calc.setEnabled(true);
					}
				});
			}
		});
		random.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				random.setEnabled(false);
				JFrame windowRandom = WindowShowRandomWalk();
				windowRandom.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						random.setEnabled(true);
					}
				});
			}
		});


		mainFrame.add(create);
		mainFrame.add(show);
		mainFrame.add(query);
		mainFrame.add(generate);
		mainFrame.add(calc);
		mainFrame.add(random);

		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setBounds(100, 100, 480, 290);
		mainFrame.setVisible(true);
	}

	public static Hashtable<String, Graph_vertex> createDirectedGraph(
			String filename) {
		Hashtable<String, Graph_vertex> g = new Hashtable<String, Graph_vertex>();
		Graph_vertex pre_vertex = null;
		Graph_vertex cur_vertex = null;
		String cur_str = "";
		String pre_str = "";
		FileReader f = null;
		try {
			f = new FileReader(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		int c = 0;
		int flag = 0;// 读取单词开始标记，读到字母置为1，操作单词之后置为0
		while (c != -1) {
			try {
				c = f.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if ((c >= 65 && c <= 90) || (c >= 97 && c <= 122)) {
				cur_str += String.valueOf((char) c);
				flag = 1;
			} else if (flag == 1) {
				cur_str = cur_str.toLowerCase();
				// 判断单词是否重复
				if (g.get(cur_str) == null) {
					cur_vertex = new Graph_vertex(cur_str);
					g.put(cur_str, cur_vertex);
				}
				cur_vertex = g.get(cur_str);
				// 第一个单词不做操作
				if (!pre_str.equals("")) {
					Node tmp_Node = pre_vertex.links;
					while (tmp_Node != null) {
						// 边已经存在，权重加1
						if (tmp_Node.link_vertex.word.equals(cur_str)) {
							tmp_Node.weight++;
							break;
						}
						tmp_Node = tmp_Node.next;
					}
					// 边不存在，添加边
					if (tmp_Node == null) {
						Node new_node = new Node(pre_vertex.links, cur_vertex);
						pre_vertex.links = new_node;
						pre_vertex.children++;
					}
				}
				pre_vertex = cur_vertex;
				pre_str = cur_str;
				cur_str = "";
				flag = 0;
			}

		}
		return g;
	}

	public static void showDirectedGraph(Hashtable<String, Graph_vertex> G) {
		String cur_word=null;
		Node tmp_node=null;
		GraphViz gViz = new GraphViz("dot.exe");
		gViz.start_graph();
		// 遍历每个单词，把每个的所有边写入脚本文件
		for (String word : G.keySet()) {
			tmp_node=G.get(word).links;
			while (tmp_node != null) {
				cur_word = tmp_node.link_vertex.word;
				gViz.addln("\"" + word + "\"->\"" + cur_word + "\"",
						tmp_node.weight);
				tmp_node = tmp_node.next;
			}
		}
		gViz.end_graph();
		try {
			gViz.run();// 由脚本文件生成图片
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String queryBridgeWords(
			Hashtable<String, Graph_vertex> G, String word1, String word2) {
		int bridgeNum = 0;// 标记桥接词数量
		StringBuffer ret = new StringBuffer(
				"The bridge words from \"" + word1 + "\" to \"" + word2
						+ "\" are: ");
		String cur_str = "";
		Graph_vertex mid_ver = null;
		Node tmp_node = null;
		// 判断word1、word2是否存在
		try {
			tmp_node = G.get(word1).links;
		} catch (Exception e) {
			return "No \"" + word1 + "\" in the graph!";
		}
		if (G.get(word2) == null) {
			return "No \"" + word2 + "\" in the graph!";
		}
		// word1->mid_ver->word2
		Node mid_node = null;// mid_ver出边指向的点
		while(tmp_node!=null) {
			mid_ver = tmp_node.link_vertex;// 可能的桥接词
			mid_node = mid_ver.links;
			cur_str = mid_ver.word;
			// 判断mid_ver指出的点是否含有word2
			while(mid_node!=null) {
				if (mid_node.link_vertex.word.equals(word2)) {
					bridgeNum++;
					ret.append(cur_str + ", ");
					break;
				}
				mid_node = mid_node.next;
			}
			tmp_node = tmp_node.next;
		}
		// 修改结果字符串
		ret.deleteCharAt(ret.length() - 1);
		int len = ret.length();
		if (bridgeNum == 0) {
			ret = new StringBuffer("No bridge words from \"" + word1
					+ "\" to \"" + word2 + "\"!");
		} else if (bridgeNum == 1) {
			ret.replace(len - 1, len, ".");
		} else {
			ret.replace(len - 1, len, ".");
			int lastpos = ret.lastIndexOf(", ");
			ret.insert(lastpos + 2, "and ");
		}
		return ret.toString();
	}

	public static String calcShortestPath(Hashtable<String, Graph_vertex> G,
			String word1, String word2) {
		// 采用深度优先遍历和分支限界法，先找到一条路径可行解，继续深度优先搜索，当前路径长度大于可行解，丢弃
		// 路径，等于可行解合并，小于更新可行解，最后的为最优解
		Stack<Node> node_sta = new Stack<Node>();// 存放下次要访问的结点
		Stack<Graph_vertex> ver_sta = new Stack<Graph_vertex>();// 路径上的点
		Stack<String> str_sta = new Stack<String>();// 路径上点的字符串
		Stack<Integer> int_sta = new Stack<Integer>();// 路径上边的长度
		Graph_vertex tmp_vertex = G.get(word2);
		spath = null;// 最短路径可行解
		// 判断word1、word2是否存在
		if (tmp_vertex == null) {
			return "\"" + word2 + "\" is not exist!";
		}
		tmp_vertex = G.get(word1);
		if (tmp_vertex == null) {
			return "\"" + word1 + "\" is not exist!";
		}
		Node tmp_node = tmp_vertex.links;
		String tmp_str = word1;
		int path_len=0;
		int cur_shortest = 0x7f7f7f7f;
		// 判断word1是否有出边
		if(tmp_node==null) {
			return "Do not arrive!";
		}
		node_sta.push(tmp_node);
		ver_sta.push(tmp_vertex);
		str_sta.push(tmp_str);
		int_sta.push(0);
		// 深度优先搜索
		while(!ver_sta.isEmpty()) {
			tmp_node=node_sta.peek();
			// 当前不能继续向下搜索或者当前路径大于可行解路径长度
			if (tmp_node == null || path_len > cur_shortest) {
				path_len -= int_sta.peek();
				ver_sta.pop();
				str_sta.pop();
				node_sta.pop();
				int_sta.pop();
			}
			// 当前结点已经存入当前路径而且当前结点不是目标结点（自环）
			else if (str_sta.contains(tmp_node.link_vertex.word) &&
					!tmp_node.link_vertex.word.equals(word2)) {
				node_sta.pop();
				node_sta.push(tmp_node.next);
			}
			// 进入下一层
			else {
				path_len += tmp_node.weight;
				tmp_vertex=tmp_node.link_vertex;
				tmp_str=tmp_vertex.word;
				node_sta.pop();
				node_sta.push(tmp_node.next);
				node_sta.push(tmp_vertex.links);
				ver_sta.push(tmp_vertex);
				str_sta.push(tmp_str);
				int_sta.push(tmp_node.weight);
				// 找到可行解
				if (tmp_str.equals(word2) && path_len <= cur_shortest) {
					if (path_len < cur_shortest) {
						cur_shortest = path_len;
						spath = null;
					}
					spath = new Short_path((Stack<String>) str_sta.clone(),
							spath);
					ver_sta.pop();
					str_sta.pop();
					node_sta.pop();
					int_sta.pop();
					path_len -= tmp_node.weight;
				}
			}
		}
		// 找到最优解
		if (cur_shortest < 0x7f7f7f7f) {
			String paths_String = "Shortest Lenth:"
					+ String.valueOf(cur_shortest) + "\n";
			Short_path tmpSPath = spath.clone();
			int i = 1;
			// 拼接路径结果
			while (tmpSPath != null) {

				paths_String += "Path" + i + ": " + PrintPath(tmpSPath.path)
						+ "\n";
				tmpSPath = tmpSPath.next;
				i++;
			}
			return paths_String;
		}
		// 无解
		else {
			return "Do not arrive!";
		}
	}

	public static String generateNewText(Hashtable<String, Graph_vertex> G,
			String inputText) {
		inputText += " ";// 保证最后一个单词有非字母字符
		byte[] chars = inputText.getBytes();
		String str1 = "";
		String str2 = "";
		String query_ret = "";
		StringBuffer new_ret = new StringBuffer();
		Hashtable<Integer, Integer> location = new Hashtable<>();// 记录每个桥接词在桥接词查询结果串中的位置
		int select_ctn = 0;
		int rand = 0;
		int flag = 0;// 为1字母读入str1，为2字母读入str2
		int len = inputText.length();
		int j = 0;
		for (int i = 0; i < len; i++) {
			if (chars[i] >= 65 && chars[i] <= 90
					|| chars[i] >= 97 && chars[i] <= 122) {
				if (flag <= 1) {
					str1 += (char) chars[i];
					flag=1;
				} else {
					str2 += (char) chars[i];
				}
			}
			// 读到的第一个单词特殊处理
			else if(flag==1) {
				new_ret.append(str1 + " ");
				flag = 2;
			}
			//str1已经读到，保证str2不为空
			else if ((!str2.equals(""))) { 
				query_ret = queryBridgeWords(G, str1, str2);// 查桥接词结果
				// 找到桥接词
				if (query_ret.indexOf(':') > 0) {
					System.out.println(query_ret);
					j = query_ret.indexOf(", and ");
					if (j > 0) {// 多个桥接词
						// 返回串中最后一个桥接词特殊处理
						location.put(select_ctn++, j + 6);
						while (query_ret.lastIndexOf(",", j - 1) > 0) {
							j = query_ret.lastIndexOf(",", j - 1);
							location.put(select_ctn, j + 1);
						}
						// 返回串中第一个桥接词特殊处理
						location.put(select_ctn++,
								query_ret.lastIndexOf(":") + 2);
						// 随机找桥接词
						long tmplong = System.currentTimeMillis()
								% select_ctn;
						rand = (int) tmplong;
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						rand = location.get(rand);
					} else {// 1个桥接词
						rand = query_ret.indexOf(":") + 2;
					}
					// rand save the index of a bridge word's begin_location
					if (query_ret.indexOf(",", rand) > 0) {
						j = query_ret.indexOf(",", rand);
					} else {
						j = query_ret.length() - 1;
					}
					// j save the index of a bridge word's end_location
					str1 = query_ret.substring(rand, j);
					new_ret.append(str1 + " ");
				}
				new_ret.append(str2 + " ");
				str1 = str2;
				str2 = "";
			}
		}
		new_ret.deleteCharAt(new_ret.length()-1);
		return new_ret.toString();
	}

	public static String randomWalk(Hashtable<String, Graph_vertex> G) {
		// StartWord类的静态变量，函数外部赋值
		Node tmp_node = null;
		// 开始单词没有出边
		if (G.get(StartWord).children == 0) {
			return "No next";
		}
		// 随机找StartWord的一个出边
		long tmplong = System.currentTimeMillis() % G.get(StartWord).children;
		int rand = (int) tmplong;
		tmp_node = G.get(StartWord).links;
		while (true) {
			if (tmp_node.weight > 0) {
				rand--;
				if (rand == -1) {
					break;
				}
			}
			tmp_node = tmp_node.next;
		}
		// 对应单词的出边减少
		G.get(StartWord).children--;
		// 标记边已经走过
		tmp_node.weight = 0;
		return tmp_node.link_vertex.word;
	}

	////////////////////////////////////////////////////////////////////

	
	public static String PrintPath(Stack<String> path_st) {
		// path_st存放路径，转为字符串存入toPrint
		StringBuffer toPrint = new StringBuffer();
		Stack<String> tmpStack = (Stack<String>) path_st.clone();
		String str1 = "";
		String str2 = tmpStack.peek();
		tmpStack.pop();
		while (!tmpStack.empty()) {
			str1 = tmpStack.peek();
			tmpStack.pop();
			toPrint.insert(0, str2);
			toPrint.insert(0, "->");
			str2 = str1;
		}
		toPrint.insert(0, str2);
		System.out.println(toPrint);
		return toPrint.toString();
	}

	
	public static String OpenFile() {
		// 选取文件弹窗
		JFileChooser file = new JFileChooser(".");
		FileFilter tmp = new FileFilter() {
			public String getDescription() {
				return "*.txt";
			}
			public boolean accept(File f) {
				String name = f.getName();
				return f.isDirectory() || name.toLowerCase().endsWith(".txt");
			}
		};

		file.addChoosableFileFilter(tmp);
		file.setFileFilter(tmp);
		// file.setAcceptAllFileFilterUsed(true);
		int result = file.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			String path = file.getSelectedFile().getAbsolutePath();
			return path;
		}
		return null;
	}

	
	public static void CreatePicture(String pictureName) {
		// 突出路径颜色
		GraphViz pr = new GraphViz("dot.exe");
		Short_path tmpPath = spath.clone();
		int i = 0;
		pr.clearTmpDotFile("ShortPath.dot");// 删除之前脚本文件
		while (tmpPath != null) {
			pr.setColorForPath(tmpPath.path,
					GraphViz.color[i % GraphViz.color.length], "ShortPath.dot");
			i++;
			tmpPath = tmpPath.next;
		}
		pr.runAfterSetColor(pictureName, "ShortPath.dot");
	}

	

	public static JFrame WindowShowPicture(String picturePath) {
		JFrame secondFrame = new JFrame("展示图片");
		JLabel picture = new JLabel();
		JPanel panel = new JPanel();
		JScrollPane sp = new JScrollPane(panel);
		File jpgFile = new File(picturePath);
		Image jpgImage = null;
		try {
			jpgImage = ImageIO.read(jpgFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		panel.add(picture);
		sp.getVerticalScrollBar().setUnitIncrement(10);
		secondFrame.setContentPane(sp);
		ImageIcon myImageIcon = new ImageIcon(jpgImage);
		picture.setIcon(myImageIcon);
		BufferedImage source = null;
		try {
			source = ImageIO.read(new FileInputStream(jpgFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		secondFrame.setSize(
				source.getWidth() + 100, source.getHeight() + 100);
		secondFrame.setLocation(800, 400);
		secondFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		secondFrame.setVisible(true);
		return secondFrame;
	}

	
	public static JFrame WindowShowShortPath() {
		JFrame thirdFrame = new JFrame("最短路径");
		JTextField textWord1 = new JTextField("", 30);
		JTextField textWord2 = new JTextField("", 30);
		JLabel labelWord1 = new JLabel("输入单词1");
		JLabel labelWord2 = new JLabel("输入单词2");
		JLabel labelOut = new JLabel("最短路径");
		JTextArea textOut = new JTextArea("", 10, 30);
		JButton showPath = new JButton("生成路径");
		JButton showPicture = new JButton("显示图片");
		JButton stop = new JButton("停止");
		JScrollPane spOut = new JScrollPane(textOut);

		thirdFrame.setLayout(null);
		labelWord1.setBounds(25, 10, 200, 40);
		labelWord2.setBounds(250, 10, 200, 40);
		textWord1.setBounds(25, 50, 200, 40);
		textWord2.setBounds(250, 50, 200, 40);
		showPath.setBounds(75, 110, 100, 30);
		stop.setBounds(185, 110, 100, 30);
		showPicture.setBounds(300, 110, 100, 30);
		labelOut.setBounds(25, 140, 200, 40);
		spOut.setBounds(25, 180, 425, 200);

		stop.setVisible(false);
		stop.setEnabled(false);
		textWord1.setFont(new Font("楷体", 1, 20));
		textWord2.setFont(new Font("楷体", 1, 20));
		textOut.setFont(new Font("楷体", 3, 20));
		textOut.setLineWrap(true);
		showPicture.setEnabled(false);

		multiShortPath = hashtableMyclone(OriginPath).keySet();
		showPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String word1 = textWord1.getText().toLowerCase();
				String word2 = textWord2.getText().toLowerCase();
				// 第一个单词为空
				if (word1.equals("")) {
					textOut.setText("请输入单词!");
				}
				// 输出 一个点到其它所有的点的最短路径
				else if (word2.equals("")
						|| showPath.getText().equals("下一个单词")) {// 处理自动填充后的情况
					// 取出下一个单词
					for (String tmp_word : multiShortPath) {
						word2 = tmp_word;
						multiShortPath.remove(word2);
						break;
					}
					showPath.setText("下一个单词");
					stop.setVisible(true);
					stop.setEnabled(true);
					textWord1.setEditable(false);
					textWord2.setEditable(false);
					textWord2.setText(word2);

					// 计算最短路径
					String shortPath = calcShortestPath(g, word1, word2);
					textOut.setText(shortPath);
					// 路径存在，生成图片
					if (shortPath.indexOf("->") >= 0) {
						showPicture.setEnabled(true);
						CreatePicture("ShortPath");
					} else {
						showPicture.setEnabled(false);
					}
				} else {// 计算指定2点之间最短路径
					String shortPath = calcShortestPath(g, word1, word2);
					textOut.setText(shortPath);
					if (shortPath.indexOf("->") >= 0) {
						showPicture.setEnabled(true);
					} else {
						showPicture.setEnabled(false);
					}
				}
				// 完成输出一个点到其它所有的点的最短路径
				if (multiShortPath.isEmpty()) {
					showPath.setText("生成路径");
					stop.setVisible(false);
					stop.setEnabled(false);
					textWord1.setEditable(true);
					textWord2.setEditable(true);
					multiShortPath = hashtableMyclone(OriginPath).keySet();
				}
			}
		});
		showPicture.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CreatePicture("ShortPath");
				String runPath = null;
				 File f = new File("");
				 try {
				 runPath = f.getCanonicalPath();
				 } catch (IOException e1) {
				 e1.printStackTrace();
				 }
				WindowShowPicture(runPath + "\\ShortPath.jpg");
			}
		});
		stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showPath.setText("生成路径");
				stop.setVisible(false);
				stop.setEnabled(false);
				textWord1.setEditable(true);
				textWord2.setEditable(true);
				multiShortPath = hashtableMyclone(OriginPath).keySet();
			}
		});

		thirdFrame.setSize(500, 430);
		thirdFrame.setLayout(null);
		thirdFrame.add(labelWord1);
		thirdFrame.add(labelWord2);
		thirdFrame.add(textWord1);
		thirdFrame.add(textWord2);
		thirdFrame.add(showPath);
		thirdFrame.add(stop);
		thirdFrame.add(showPicture);
		thirdFrame.add(labelOut);
		thirdFrame.add(spOut);
		thirdFrame.dispose();
		thirdFrame.setVisible(true);
		return thirdFrame;
	}

	
	public static JFrame WindowShowNewText() {
		JFrame forthFrame = new JFrame("生成新文本");
		JLabel labelIn = new JLabel("输入新文本");
		JLabel labelOut = new JLabel("生成新文本");
		JTextArea textIn = new JTextArea("", 10, 30);
		JTextArea textOut = new JTextArea("", 10, 30);
		JButton createNew = new JButton("开始生成");
		JScrollPane spIn = new JScrollPane(textIn);
		JScrollPane spOut = new JScrollPane(textOut);

		forthFrame.setLayout(null);
		labelIn.setBounds(25, 10, 200, 40);
		spIn.setBounds(25, 50, 425, 150);
		createNew.setBounds(170, 225, 100, 30);
		labelOut.setBounds(25, 255, 200, 40);
		spOut.setBounds(25, 295, 425, 150);

		textIn.setFont(new Font("楷体", 1, 20));
		textOut.setFont(new Font("楷体", 1, 20));
		textIn.setLineWrap(true);
		textOut.setLineWrap(true);
		createNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String originText = textIn.getText().toLowerCase();
				if (originText.equals("")) {
					textOut.setText("请输入文本!");
				} else {
					String newText = generateNewText(g, originText);
					textOut.setText(newText);
				}
			}
		});

		forthFrame.setSize(500, 550);
		forthFrame.add(labelIn);
		forthFrame.add(spIn);
		forthFrame.add(createNew);
		forthFrame.add(labelOut);
		forthFrame.add(spOut);
		forthFrame.dispose();
		forthFrame.setVisible(true);
		return forthFrame;

	}

	
	public static JFrame WindowShowQueryBridge() {
		JFrame fifthFrame = new JFrame("查询桥接词");
		JTextField textWord1 = new JTextField("", 30);
		JTextField textWord2 = new JTextField("", 30);
		JLabel labelWord1 = new JLabel("输入单词1");
		JLabel labelWord2 = new JLabel("输入单词2");
		JLabel labelOut = new JLabel("桥接词查询结果");
		JTextArea textOut = new JTextArea("", 10, 30);
		JButton startQuery = new JButton("开始查询");
		JScrollPane spOut = new JScrollPane(textOut);

		fifthFrame.setLayout(null);
		labelWord1.setBounds(25, 10, 200, 40);
		labelWord2.setBounds(250, 10, 200, 40);
		textWord1.setBounds(25, 50, 200, 40);
		textWord2.setBounds(250, 50, 200, 40);
		startQuery.setBounds(175, 110, 100, 30);
		labelOut.setBounds(25, 140, 200, 40);
		spOut.setBounds(25, 180, 425, 200);

		textWord1.setFont(new Font("楷体", 1, 20));
		textWord2.setFont(new Font("楷体", 1, 20));
		textOut.setFont(new Font("楷体", 3, 20));
		textOut.setLineWrap(true);
		startQuery.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String word1 = textWord1.getText().toLowerCase();
				String word2 = textWord2.getText().toLowerCase();
				// 保证单词纯英文
				word1.matches("[a-zA-Z]+");
				if (word1.equals("") || word2.equals("")) {
					textOut.setText("请输入单词!");
				} else if (word1.matches("[a-zA-Z]+")
						&& word2.matches("[a-zA-Z]+")) {
					String tmpString = queryBridgeWords(g, word1, word2);
					textOut.setText(tmpString);
				} else {
					textOut.setText("仅输入英文!");
				}
			}
		});

		fifthFrame.setSize(500, 430);
		fifthFrame.setLayout(null);
		fifthFrame.add(labelWord1);
		fifthFrame.add(labelWord2);
		fifthFrame.add(textWord1);
		fifthFrame.add(textWord2);
		fifthFrame.add(startQuery);
		fifthFrame.add(labelOut);
		fifthFrame.add(spOut);
		fifthFrame.dispose();
		fifthFrame.setVisible(true);
		return fifthFrame;

	}

	
	public static JFrame WindowShowRandomWalk() {
		int width = 800;
		int height = 900;
		JFrame RandomFrame = new JFrame("随机游走");
		JLabel picture = new JLabel();
		JPanel panel = new JPanel();
		JScrollPane sp = new JScrollPane(panel);
		JButton nextWalk = new JButton("开始");
		JButton stopWalk = new JButton("停止");
		sp.getVerticalScrollBar().setUnitIncrement(10);
		RandomFrame.setLayout(null);
		nextWalk.setBounds(width / 4, height / 20, 100, 30);
		stopWalk.setBounds(width / 2, height / 20, 100, 30);
		sp.setBounds(0, height / 10, width - 20, height / 5 * 4 + 30);
		stopWalk.setEnabled(false);

		nextWalk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(circulate==0) {
					GraphViz gra = new GraphViz("dot.exe");
					gra.clearTmpDotFile("RandomWalk.dot");
					path = hashtableMyclone(OriginPath);
					Enumeration<String> words = g.keys();
					// 随机生成起始单词
					long tmplong = System.currentTimeMillis() % path.size();
					int rand = (int) tmplong;
					for (; rand > -1; rand--) {
						StartWord = words.nextElement();
					}
					nextWalk.setText("下一步");
					stopWalk.setEnabled(true);
					System.out.println("newPath:");
					System.out.println(StartWord);
				}
				circulate=1;
				Stack<String> path_ver = new Stack<String>();// 存放路径经过的点
				path_ver.push(StartWord);
				String word2=randomWalk(path);
				String targetPath = OriginPath.substring(0,
						OriginPath.lastIndexOf("\\") + 1);// 得到存放脚本和图片的路径
				// 有下一个单词并向下走一步
				if(!word2.equals("No next")) {
					System.out.println(word2);
					path_ver.push(word2);
					GraphViz gra = new GraphViz("dot.exe");
					gra.setColorForPath(path_ver, "red", "RandomWalk.dot");
					gra.runAfterSetColor("RandomWalk", "RandomWalk.dot");
					File jpgFile = new File(targetPath + "RandomWalk.jpg");
					Image jpgImage = null;
					try {
						jpgImage = ImageIO.read(jpgFile);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					panel.add(picture);
					picture.setIcon(new ImageIcon(jpgImage));
					// 更新起始单词
					StartWord = word2;
				}
				// 没有路可走，显示原始图片
				else {
					File jpgFile = new File(targetPath + "graph.jpg");
					Image jpgImage = null;
					try {
						jpgImage = ImageIO.read(jpgFile);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					panel.add(picture);
					picture.setIcon(new ImageIcon(jpgImage));
					circulate=0;
					nextWalk.setText("开始");
					stopWalk.setEnabled(false);
				}
			}
		});
		stopWalk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				circulate = 0;
				nextWalk.setText("开始");
				stopWalk.setEnabled(false);
			}
		});

		RandomFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				circulate = 0;
				nextWalk.setText("开始");
				stopWalk.setEnabled(false);
			}
		});

		RandomFrame.setSize(width, height);
		RandomFrame.setLayout(null);
		RandomFrame.add(nextWalk);
		RandomFrame.add(stopWalk);
		RandomFrame.add(sp);
		RandomFrame.dispose();
		RandomFrame.setVisible(true);
		RandomFrame.setResizable(false);
		return RandomFrame;
	}

	
	public static Hashtable<String, Graph_vertex> hashtableMyclone(
			String filename) {
		Hashtable<String, Graph_vertex> ret = new Hashtable<String, Graph_vertex>();
		Graph_vertex pre_vertex = null;
		Graph_vertex cur_vertex = null;
		String cur_str = "";
		String pre_str = "";
		FileReader f = null;
		try {
			f = new FileReader(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		int c = 0;
		int flag = 0;
		while (c != -1) {
			try {
				c = f.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if ((c >= 65 && c <= 90) || (c >= 97 && c <= 122)) {
				cur_str += String.valueOf((char) c);
				flag = 1;
			} else if (flag == 1) {
				// System.out.println(cur_str);
				cur_str = cur_str.toLowerCase();
				if (ret.get(cur_str) == null) {
					cur_vertex = new Graph_vertex(cur_str);
					ret.put(cur_str, cur_vertex);
				}
				cur_vertex = ret.get(cur_str);
				if (!pre_str.equals("")) {
					Node tmp_Node = pre_vertex.links;
					while (tmp_Node != null) {
						if (tmp_Node.link_vertex.word.equals(cur_str)) {
							tmp_Node.weight++;
							break;
						}
						tmp_Node = tmp_Node.next;
					}
					if (tmp_Node == null) {
						Node new_node = new Node(pre_vertex.links, cur_vertex);
						pre_vertex.links = new_node;
						pre_vertex.children++;
					}
				}
				pre_vertex = cur_vertex;
				pre_str = cur_str;
				cur_str = "";
				flag = 0;
			}

		}
		return ret;
		}
}// D:\Java\project\Pair_txt\test.txt

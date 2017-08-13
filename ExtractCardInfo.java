package name.frw;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ExtractCardInfo extends JFrame {
	private static final long serialVersionUID = -7844908032131837091L;
	private static final String VERSION_NO = "5";
	private static final int HEIGHT_OF_WINDOW = 250;
	
	private final JTextField fileField = new JTextField("<folder path>\\CardDefs.xml");
	private final JRadioButton debugRButton = new JRadioButton("Debug mode", false);
	private final JLabel resultLabel = new JLabel("You need to download CardDefs.xml from https://github.com/HearthSim/hs-data");
	private final JButton startButton = new JButton("Start");
	
	private static ExtractCardInfo tm;

	private static PrintWriter pw;

	private ExtractCardInfo() {
		super("Extract Card Information from hs-data" + " v" + VERSION_NO);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String sourceFile = fileField.getText();

				try {
					if (sourceFile.equals("")) {
						throw new Exception("blank file path.");
					}

					pw = new PrintWriter("cardDB.txt");
//					pw.println("Cost\tName\tClass\tRarity\tCardSet\tCardID\tCardType");
					pw.println("Cost\tName\tClass\tRarity\tCardSet\tCardType");
					extract(sourceFile);
					pw.close();

					JOptionPane.showMessageDialog(null, "Done.");
				} catch (Exception exception) {
					JOptionPane.showMessageDialog(null, "Error happens:\n" + exception.getMessage());
				}
			}

			private String nodeToString(Node node) {
				StringWriter sw = new StringWriter();
				try {
					Transformer t = TransformerFactory.newInstance().newTransformer();
					t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
					t.transform(new DOMSource(node), new StreamResult(sw));
				} catch (TransformerException te) {
					JOptionPane.showMessageDialog(null, "Error happens:\n" + te.getMessage());
				}
				return sw.toString();
			}

			private String changeValue2Class(String value) {
				String text = "";

				if (value.equals("2")) {
					text = "Druid";
				} else if (value.equals("3")) {
					text = "Hunter";
				} else if (value.equals("4")) {
					text = "Mage";
				} else if (value.equals("5")) {
					text = "Paladin";
				} else if (value.equals("6")) {
					text = "Priest";
				} else if (value.equals("7")) {
					text = "Rogue";
				} else if (value.equals("8")) {
					text = "Shaman";
				} else if (value.equals("9")) {
					text = "Warlock";
				} else if (value.equals("10")) {
					text = "Warrior";
				} else if (value.equals("12")) {
					text = "Neutral";
				}

				return checkDebugMode(value, text);
			}

			private String changeValue2Rarity(String value, String cardSet) {
				if (cardSet.equals("2")) {
					return checkDebugMode(value, "Basic");
				}
				
				String text = "";

				if (value.equals("1")) {
					text = "Common";
				} else if (value.equals("2")) {
					text = "Basic";
				} else if (value.equals("3")) {
					text = "Rare";
				} else if (value.equals("4")) {
					text = "Epic";
				} else if (value.equals("5")) {
					text = "Legendary";
				}

				return checkDebugMode(value, text);
			}

			private String changeValue2CardType(String value) {
				String text = "";

				if (value.equals("4")) {
					text = "Minion";
				} else if (value.equals("5")) {
					text = "Spell";
				} else if (value.equals("7")) {
					text = "Weapon";
				} else if (value.equals("3")) {
					text = "Hero";
				}

				return checkDebugMode(value, text);
			}

			private String checkDebugMode(String value, String text) {
				if (debugRButton.isSelected()) {
					return value + text;
				}
				return text;
			}

			private void extract(String sourceFile) throws Exception {
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(sourceFile));
				XPath xpath = XPathFactory.newInstance().newXPath();
				XPathExpression expr = xpath.compile("/CardDefs/Entity[Tag[@enumID='321' and @value='1']]");
				NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
				for (int i = 0; i < nodes.getLength(); i++) {
					Element entity = (Element) nodes.item(i);
					doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(nodeToString(entity))));

					String cardCost = xpath.compile("/Entity/Tag[@enumID='48']/@value").evaluate(doc);
					if (cardCost.equals("")) {
						cardCost = "0";
					}
					String cardName = xpath.compile("/Entity/Tag[@enumID='185']/enUS").evaluate(doc);
					String cardClass = xpath.compile("/Entity/Tag[@enumID='199']/@value").evaluate(doc);
					String cardCardType = xpath.compile("/Entity/Tag[@enumID='202']/@value").evaluate(doc);
					String cardRarity = xpath.compile("/Entity/Tag[@enumID='203']/@value").evaluate(doc);
					String cardCardSet = xpath.compile("/Entity/Tag[@enumID='183']/@value").evaluate(doc);
//					String cardID = entity.getAttribute("CardID");

//					String text = cardCost + "\t" + cardName + "\t" + changeValue2Class(cardClass) + "\t" + changeValue2Rarity(cardRarity, cardCardSet) + "\t" + cardCardSet + "\t" + cardID + "\t" + changeValue2CardType(cardCardType);
					String text = cardCost + "\t" + cardName + "\t" + changeValue2Class(cardClass) + "\t" + changeValue2Rarity(cardRarity, cardCardSet) + "\t" + cardCardSet + "\t" + changeValue2CardType(cardCardType);
					pw.println(text);
				}
			}
		});

		setLayout(new GridLayout(0, 1));
		add(new JLabel("File path:"));
		add(fileField);
		add(debugRButton);

		add(resultLabel);
		add(startButton);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				tm = new ExtractCardInfo();
				tm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				tm.setSize(600, HEIGHT_OF_WINDOW);
				tm.setLocationRelativeTo(null);
				tm.setVisible(true);
			}
		});
	}
}

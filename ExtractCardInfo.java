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
	private static final String VERSION_NO = "3";
	private static final int HEIGHT_OF_WINDOW = 250;
	private final JTextField fileField = new JTextField("");

	private final JLabel resultLabel = new JLabel("You need to download CardDefs.xml from https://github.com/HearthSim/hs-data");
	private final JButton startButton = new JButton("Start");
	private static ExtractCardInfo tm;

	private static String sourceFile;

	private ExtractCardInfo() {
		super("Extract Card Information from hs-data" + " v" + VERSION_NO);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sourceFile = fileField.getText();

				try {
					if (sourceFile.equals("")) {
						throw new Exception();
					}

					PrintWriter pw = new PrintWriter("cardDB.txt");
					pw.println("Cost\tName\tClass\tRarity\tCardSet\tCardID\tCardType");
					extract(pw);
					pw.close();

					JOptionPane.showMessageDialog(null, "Done.");
				} catch (Exception exception) {
					JOptionPane.showMessageDialog(null, "Error happens.");
					System.out.println(exception);
				}
			}

			private String nodeToString(Node node) {
				StringWriter sw = new StringWriter();
				try {
					Transformer t = TransformerFactory.newInstance().newTransformer();
					t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
					t.transform(new DOMSource(node), new StreamResult(sw));
				} catch (TransformerException te) {
					System.out.println("nodeToString Transformer Exception");
				}
				return sw.toString();
			}

			private String addText2Class(String value) {
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
					text = "Prist";
				} else if (value.equals("7")) {
					text = "Rogue";
				} else if (value.equals("8")) {
					text = "Shaman";
				} else if (value.equals("9")) {
					text = "Warlock";
				} else if (value.equals("10")) {
					text = "Warrior";
				} else if (value.equals("12")) {
					text = "Natural";
				}

				return value + text;
			}

			private String addText2Rarity(String value) {
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
					text = "Legindary";
				}

				return value + text;
			}

			private String addText2CardType(String value) {
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

				return value + text;
			}

			private void extract(PrintWriter pw) throws Exception {
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(sourceFile));
				XPath xpath = XPathFactory.newInstance().newXPath();
				XPathExpression expr = xpath.compile("/CardDefs/Entity");
				NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
				for (int i = 0; i < nodes.getLength(); i++) {
					Element entity = (Element) nodes.item(i);
					doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(nodeToString(entity))));

					// Collectible?
					xpath = XPathFactory.newInstance().newXPath();
					String cardCollectible = xpath.compile("/Entity/Tag[@name='Collectible']/@value").evaluate(doc);
					if (!cardCollectible.equals("1")) {
						continue;
					}
					// Hero?
					xpath = XPathFactory.newInstance().newXPath();
					String cardCardType = xpath.compile("/Entity/Tag[@name='CardType']/@value").evaluate(doc);
					if (cardCardType.equals("3")) {
						continue;
					}

					String cardCost = xpath.compile("/Entity/Tag[@name='Cost']/@value").evaluate(doc);
					String cardName = xpath.compile("/Entity/Tag[@name='CardName']/enUS").evaluate(doc);
					String cardClass = addText2Class(xpath.compile("/Entity/Tag[@name='Class']/@value").evaluate(doc));
					String cardRarity = addText2Rarity(xpath.compile("/Entity/Tag[@name='Rarity']/@value").evaluate(doc));
					String cardCardSet = xpath.compile("/Entity/Tag[@name='CardSet']/@value").evaluate(doc);
					String cardID = entity.getAttribute("CardID");
					cardCardType = addText2CardType(cardCardType);

					pw.println(cardCost + "\t" + cardName + "\t" + cardClass + "\t" + cardRarity + "\t" + cardCardSet + "\t" + cardID + "\t" + cardCardType);
				}
			}
		});

		setLayout(new GridLayout(0, 1));
		add(new JLabel("File path:"));
		add(fileField);

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

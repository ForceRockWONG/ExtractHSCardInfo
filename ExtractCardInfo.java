import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ExtractCardInfo extends JFrame {
	private static final long serialVersionUID = 2L;
	private static final int HEIGHT_OF_WINDOW = 250;
	private final JTextField fileField = new JTextField("");

	private final JLabel resultLabel = new JLabel("You need to download the xml from https://github.com/HearthSim/hs-data/CardDefs.xml");
	private final JButton startButton = new JButton("Start");
	private static ExtractCardInfo tm;

	private static String sourceFile;

	private ExtractCardInfo() {
		super("Extract Card Information from hs-data" + " v" + serialVersionUID);
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

			private void extract(PrintWriter pw) throws Exception {
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(sourceFile));
				XPath xpath = XPathFactory.newInstance().newXPath();
				XPathExpression expr = xpath.compile("//Entity");
				NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
				for (int i = 0; i < nodes.getLength(); i++) {
					Node entity = nodes.item(i);
					// CardID in <Entity>
					String cardID = entity.getAttributes().getNamedItem("CardID").getNodeValue();

					try {
						// Collectible
						expr = xpath.compile("//Entity[@CardID='" + cardID + "']/Tag[@name='Collectible']");
						String cardCollectible = ((NodeList) expr.evaluate(doc, XPathConstants.NODESET)).item(0).getAttributes().getNamedItem("value").getNodeValue();
						if (!cardCollectible.equals("1")) {
							continue;
						}

						// CardName
						expr = xpath.compile("//Entity[@CardID='" + cardID + "']/Tag[@name='CardName']/enUS");
						String cardName = (String) expr.evaluate(doc, XPathConstants.STRING);

						// Cost , Class , Rarity , CardSet , CardType
						expr = xpath.compile("//Entity[@CardID='" + cardID + "']/Tag[@name='Cost']");
						String cardCost = ((NodeList) expr.evaluate(doc, XPathConstants.NODESET)).item(0).getAttributes().getNamedItem("value").getNodeValue();
						String cardClass = "";
						try {
							expr = xpath.compile("//Entity[@CardID='" + cardID + "']/Tag[@name='Class']");
							cardClass = ((NodeList) expr.evaluate(doc, XPathConstants.NODESET)).item(0).getAttributes().getNamedItem("value").getNodeValue();
						} catch (Exception eCardClass) {
							cardClass = "1";
						}
						expr = xpath.compile("//Entity[@CardID='" + cardID + "']/Tag[@name='Rarity']");
						String cardRarity = ((NodeList) expr.evaluate(doc, XPathConstants.NODESET)).item(0).getAttributes().getNamedItem("value").getNodeValue();
						expr = xpath.compile("//Entity[@CardID='" + cardID + "']/Tag[@name='CardSet']");
						String cardCardSet = ((NodeList) expr.evaluate(doc, XPathConstants.NODESET)).item(0).getAttributes().getNamedItem("value").getNodeValue();
						expr = xpath.compile("//Entity[@CardID='" + cardID + "']/Tag[@name='CardType']");
						String cardCardType = ((NodeList) expr.evaluate(doc, XPathConstants.NODESET)).item(0).getAttributes().getNamedItem("value").getNodeValue();

						pw.println(cardCost + "\t" + cardName + "\t" + cardClass + "\t" + cardRarity + "\t" + cardCardSet + "\t" + cardID + "\t" + cardCardType);
					} catch (Exception e) {
					}
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

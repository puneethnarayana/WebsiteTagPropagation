package com.iiitb.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.http.client.ClientProtocolException;

import com.iiitb.model.SeedHandler;
import com.iiitb.wtp.Parser;
import com.mysql.jdbc.Connection;
/**
 * 
 * @author Amulya Kishore
 *
 */
public class UserInterface extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	JButton enterSeed = new JButton("ENTER SEED");
	JButton startCrawl = new JButton("START WEBCRAWLING");
	JButton query = new JButton("QUERY");

	JLabel userseed;
	JLabel userseed_wiki;
	JLabel query_website;
	JLabel init_tag;

	SeedHandler objSeedHandler = null;
	Connection conn = null;

	private JPanel contentPane;
	private JPanel contentPane1;
	private JTextField url;
	private JTextField wikiObject;
	private JTextArea queryURL;
	private JTextField initialTag;

	/**
	 * The userinterface constructor which sets the layout for the user to
	 * interact with the program
	 */
	public UserInterface() {

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setBounds(100, 100, 500, 350);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		userseed = new JLabel("URL");
		userseed.setBounds(70, 89, 50, 25);
		contentPane.add(userseed);

		url = new JTextField();
		url.setBounds(162, 89, 250, 25);
		contentPane.add(url);
		url.setColumns(10);

		userseed_wiki = new JLabel("Wiki Object");
		userseed_wiki.setBounds(70, 125, 70, 25);
		contentPane.add(userseed_wiki);

		wikiObject = new JTextField();
		wikiObject.setBounds(162, 125, 250, 25);
		contentPane.add(wikiObject);
		wikiObject.setColumns(10);

		init_tag = new JLabel("Initial tag");
		init_tag.setBounds(70, 180, 120, 25);
		contentPane.add(init_tag);

		initialTag = new JTextField();
		initialTag.setBounds(162, 180, 250, 25);
		contentPane.add(initialTag);
		initialTag.setColumns(10);

		// creating the object of SeedHandler and loading the seed data from the
		// database
		try {
			objSeedHandler = new SeedHandler();
			objSeedHandler.loadData();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		JLabel lblXcrawler = new JLabel("WEBSITE TAG PROPAGATION");
		lblXcrawler.setBounds(135, 30, 250, 14);
		contentPane.add(lblXcrawler);

		startCrawl.setBounds(180, 250, 175, 23);
		contentPane.add(startCrawl);
		startCrawl.addActionListener(this);

		enterSeed.setBounds(50, 250, 125, 23);
		contentPane.add(enterSeed);
		enterSeed.addActionListener(this);

		query.setBounds(360, 250, 111, 23);
		contentPane.add(query);
		query.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {

		String tagResult;
		// Start crawling with input seed data
		if (e.getSource() == startCrawl) {
			System.out.println("Webcrawling started!");

			try {
				if (!url.getText().equals("")) {

					URL connect = new URL(url.getText());
					URLConnection yc = connect.openConnection();
					objSeedHandler.insertData(url.getText(),
							initialTag.getText());
					initialTag.setText("");
					url.setText("");
				} else if (!wikiObject.getText().equals("")) {
					wikiObject.setText(wikiObject.getText().toLowerCase());
					String[] split = wikiObject.getText().trim().split("\\s+");
					String str = null;
					String inString = "";
					for (String string : split) {
						str = Character.toString(string.charAt(0))
								.toUpperCase() + string.substring(1);
						str = str + "_";
						inString = inString + str;
					}

					inString = inString.substring(0, inString.length() - 1);
					String url = "http://en.wikipedia.org/wiki/" + inString;
					URL connect = new URL(url);
					URLConnection yc = connect.openConnection();

					objSeedHandler.insertData(url, initialTag.getText());
					initialTag.setText("");
					wikiObject.setText("");

				}

				objSeedHandler.mainCrawler();
			} catch (SQLException e1) {

				e1.printStackTrace();
			} catch (MalformedURLException e1) {

				e1.printStackTrace();
			} catch (IOException e1) {

				e1.printStackTrace();
			}
		}
		//Run the simulation on the already crawled data
		else if (e.getSource() == query) {

			Parser p = new Parser();
			try {
				p.calcProbDeserialized();

			} catch (ClientProtocolException e1) {
				e1.printStackTrace();
			} catch (SQLException e2) {
				e2.printStackTrace();
			} catch (IOException e3) {
				e3.printStackTrace();
			} catch (ClassNotFoundException e4) {
				e4.printStackTrace();
			}
		} 
		// Once user click this, the seed will be entered in DB
		else if (e.getSource() == enterSeed) {

			try {
				if (url.getText().equals("") && wikiObject.getText().equals("")) {
					JOptionPane.showMessageDialog(contentPane,
							"Enter the url or a wiki object name ");
				} else if (wikiObject.getText().equals("")) {

					URL connect = new URL(url.getText());
					URLConnection yc = connect.openConnection();
					objSeedHandler.insertData(url.getText(),
							initialTag.getText());
					initialTag.setText("");

					url.setText("");
				} else {
					wikiObject.setText(wikiObject.getText().toLowerCase());
					String[] split = wikiObject.getText().trim().split("\\s+");
					String str = null;
					String inString = "";
					for (String string : split) {
						str = Character.toString(string.charAt(0))
								.toUpperCase() + string.substring(1);
						str = str + "_";
						inString = inString + str;
					}
					inString = inString.substring(0, inString.length() - 1);
					String url = "http://en.wikipedia.org/wiki/" + inString;
					URL connect = new URL(url);
					URLConnection yc = connect.openConnection();

					objSeedHandler.insertData(url, initialTag.getText());
					initialTag.setText("");
					wikiObject.setText("");

				}

			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

	}

}

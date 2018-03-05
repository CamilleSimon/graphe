package TP4;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BoxLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import org.graphstream.algorithm.APSP;
import org.graphstream.algorithm.APSP.APSPInfo;
import org.graphstream.graph.Edge;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;



/**
 * @author Ballot Corentin & Simon Camille
 * 16 mars 2017
 */
/**
 * @author Corentin Ballot
 *
 */
public class Fenetre extends JFrame {

	private static final long serialVersionUID = 1L;

	Graph g = new SingleGraph("Network");

	JTextField add_node_tf = new JTextField("");							// Champs du nom du noeud à ajouter
	JTextField load_file_tf = new JTextField("");							// Champs du chemin du fichier chargé
	JComboBox<String> remove_node_cb = new JComboBox<String>();				// Liste déroulante pour selectionner un noeud à supprimer
	JComboBox<String> remove_edge_cb = new JComboBox<String>();				// Liste déroulante pour selectionner une arete à supprimer
    JComboBox<String> start_node_cb = new JComboBox<String>();				// Liste déroulante pour selectionner le noeud de départ de l'arete à créer
    JComboBox<String> end_node_cb = new JComboBox<String>();				// Liste déroulante pour selectionner le noeud d'arrivé de l'arete à créer
    JComboBox<String> routing_from_node_cb = new JComboBox<String>();		// Liste déroulante pour selectionner le noeud de départ pour le calcul du plus court chemin
    JComboBox<String> routing_to_node_cb = new JComboBox<String>();			// Liste déroulante pour selectionner le noeud de d'arrivé pour le calcul du plus court chemin
    JComboBox<String> routing_table_node_cb = new JComboBox<String>();		// Liste déroulante pour selectionner le noeud dont on afficher la table de routage
    JSpinner edge_weight_sp = new JSpinner();								// Champ de selection du poid des aretes
    JTable routing_table_jt = new JTable(new RoutingTableModel());			// Table de routage

    ArrayList<JComboBox<String>> nodeLists = new ArrayList<JComboBox<String>>();	// ArrayList des listes déroulantes contenant la liste des noeuds
    
    String style = "graph {	padding: 40px;}\n"
    				+ "node {size: 5px; text-size: 14px; text-offset: 0px, 15px; text-color: black; text-alignment: at-right; fill-color: blue;}\n"
    				+ "edge {fill-color: #c0c0c0; size: 2px;}\n"
    				+ "node.pcc {size: 7px; fill-color: red;}\n"
    				+ "edge.pcc {fill-color: red;}\n";

	/**
	 * Notre fenêtre
	 */
	public Fenetre(){
		// Initialisation du graphe
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		g.addAttribute("ui.quality");
		g.addAttribute("ui.antialias");
		//g.addAttribute("ui.stylesheet", "url('../style.css')");
		g.addAttribute("ui.stylesheet", style);

		// Ajout des listes déroulates contenant la liste des noeuds à l'ArrayList
		nodeLists.add(remove_node_cb);
		nodeLists.add(start_node_cb);
		nodeLists.add(end_node_cb);
		nodeLists.add(routing_from_node_cb);
		nodeLists.add(routing_to_node_cb);
		nodeLists.add(routing_table_node_cb);

		// Paramètres de la fenêtre
		this.setTitle("TP4 - Le routage"); 		// Nom de la fenêtre
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);			// Tue le processus lorsque l'on quitte l'application
		this.setLayout(new BorderLayout());								// Définit le layout de la fenêtre

		drawForm();
		drawRootingTable();
		drawNetwork();

		this.pack();
		this.setMinimumSize(new Dimension(this.getWidth(), this.getHeight()));
		this.setVisible(true);
		this.setLocationRelativeTo(null);								// Ouvre la fenêtre au milieu de l'écran
	}

	/**
	 * Ajoute dans le BorderLayout.CENTER de notre JFrame le panel contenant la representation de notre réseau
	 */
	private void drawNetwork() {
		// Initialisation de notre panel
		JPanel panel = new JPanel(new GridLayout(1, 1));

		// Ajout d'un cadre autour de notre panel avec le titre : "Network graph"
		TitledBorder panel_title = BorderFactory.createTitledBorder("Network graph");
		panel_title.setTitleJustification(TitledBorder.LEFT);
	    panel.setBorder(panel_title);
	    panel.setPreferredSize(new Dimension(500, 300));

	    // Récupération du composant dans lequel est dessiné le graph
	    Viewer viewer = new Viewer(g, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
	    viewer.enableAutoLayout();
        View view = viewer.addDefaultView(false);

        // Ajout du composant dans lequel est dessiné le graph à notre panel
        panel.add((Component) view);

        // Ajout de notre panel au centre de notre fenetre
	    this.add(panel, BorderLayout.CENTER);
	}

	
	/**
	 * Ajoute dans le BorderLayout.EAST de notre JFrame le panel contenant le formulaire de selection des noeuds 
	 * dont on evaluera le plus court chemin, et l'affichage de la table de routage d'un noeud
	 */
	private void drawRootingTable() {
		// Initialisation de notre panel
		JPanel panel = new JPanel(new BorderLayout());
		// Ajout d'un cadre autour de notre panel avec le titre : "Path info"
		TitledBorder panel_title = BorderFactory.createTitledBorder("Path info");
		panel_title.setTitleJustification(TitledBorder.LEFT);
	    panel.setBorder(panel_title);
	    panel.setPreferredSize(new Dimension(300, 300));

	    // Sous-panel contenant le formulaire de selection des noeuds dont on evaluera le plus court chemin
	    JPanel panel_routing_nodes = new  JPanel();
	    panel_routing_nodes.setLayout(new BoxLayout(panel_routing_nodes, BoxLayout.PAGE_AXIS));
	    // Ajout d'un cadre autour de notre sous-panel avec le titre : "Min path between nodes"
	    TitledBorder panel_routing_nodes_title = BorderFactory.createTitledBorder("Min path between nodes");
	    panel_routing_nodes_title.setTitleJustification(TitledBorder.LEFT);
	    panel_routing_nodes.setBorder(panel_routing_nodes_title);
	    // Sous-sous-panel contenant les listes déroulantes
	    JPanel panel_select_routing_nodes = new  JPanel();
	    panel_select_routing_nodes.setLayout(new BoxLayout(panel_select_routing_nodes, BoxLayout.LINE_AXIS));
	    // Définition des dimension préférées de nos listes déroulantes
	    routing_from_node_cb.setPreferredSize(new Dimension(240, 25));
	    routing_to_node_cb.setPreferredSize(new Dimension(240, 25));
	    // Ajout des listes déroulantes au sous-sous-panel
	    panel_select_routing_nodes.add(routing_from_node_cb);
	    panel_select_routing_nodes.add(routing_to_node_cb);
	    // Initialisation du bouton "Valider"
	    JButton routing_jbtn = new JButton("Valider");
	    routing_jbtn.addActionListener(new displayPlusCourtChemin());	// Ajout d'un écouteur à notre bouton
	    // Ajout à notre sous-panel les composants créés
	    panel_routing_nodes.add(panel_select_routing_nodes);
	    panel_routing_nodes.add(routing_jbtn);
	    
	    // Sous-panel contenant la table de routage
	    JPanel panel_routing_table_node = new  JPanel(new BorderLayout());
	    // Ajout d'un cadre autour de notre sous-panel avec le titre : "Routing table"
	    TitledBorder panel_routing_table_node_title = BorderFactory.createTitledBorder("Routing table");
	    panel_routing_table_node_title.setTitleJustification(TitledBorder.LEFT);
	    panel_routing_table_node.setBorder(panel_routing_table_node_title);
	    // Parametrage des compsants
	    routing_table_node_cb.setPreferredSize(new Dimension(400, 25)); 	// Définition des dimension préférées de notre liste déroulante
	    routing_table_node_cb.addActionListener(new updateRoutingTable());	// Ajout d'un écouteur à notre liste déroulante
	    routing_table_jt.setTableHeader(null);								// Retire l'entête du JTable contenant la table de routage
	    JScrollPane routing_table_jsp = new JScrollPane(routing_table_jt);	// Définition d'un JScollPane qui englobe notre JTable
	    routing_table_jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);		// N'affiche la scroll bar vertical qu'en cas de necessité
	    routing_table_jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);	// N'affiche la scroll bar horizontal qu'en cas de necessité
	    routing_table_jsp.setBorder(new EmptyBorder(0, 0, 0, 0));
	    // Ajout à notre sous-panel la liste déroulante et le JScrollPane contant notre JTable
	    panel_routing_table_node.add(routing_table_node_cb, BorderLayout.NORTH);
	    panel_routing_table_node.add(routing_table_jsp, BorderLayout.CENTER);
	    
	    // Ajout à notre panel des deux sous-panel créés ci-dessus
	    panel.add(panel_routing_nodes, BorderLayout.NORTH);
	    panel.add(panel_routing_table_node, BorderLayout.CENTER);

	    // Ajout de notre panel à l'est de notre fenetre
	    this.add(panel, BorderLayout.EAST);

	}

	/**
	 * Ajoute dans le BorderLayout.WEST de notre JFrame le panel contenant les formulaires 
	 * d'ajout / suppression de noeuds et arêtes, et de chargement de fichier "dgs"
	 */
	private void drawForm() {
		// Initialisation de notre panel
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		// Ajout d'un cadre autour de notre panel avec le titre : "Settings"
		TitledBorder panel_title = BorderFactory.createTitledBorder("Settings");
		panel_title.setTitleJustification(TitledBorder.LEFT);
	    panel.setBorder(panel_title);

	    // Sous-panel d'ajout d'un noeud
	    JPanel panel_add_node = new  JPanel();
	    panel_add_node.setLayout(new BoxLayout(panel_add_node, BoxLayout.LINE_AXIS));
	    TitledBorder panel_add_node_title = BorderFactory.createTitledBorder("Add node");
	    panel_add_node_title.setTitleJustification(TitledBorder.LEFT);
	    panel_add_node.setBorder(panel_add_node_title);
	    panel_add_node.setPreferredSize(new Dimension(400, 48));
	    panel_add_node.setMaximumSize(new Dimension(400, 48));
	    add_node_tf.setPreferredSize(new Dimension(240, 25));
	    add_node_tf.addActionListener(new addNode());
	    JButton add_node_btn = new JButton("Add");
	    add_node_btn.addActionListener(new addNode());
	    add_node_btn.setPreferredSize(new Dimension(90, 25));
	    // Ajout des composants au sous-panel
	    panel_add_node.add(add_node_tf);
	    panel_add_node.add(add_node_btn);

	    // Sous-panel de suppression d'un noeud
	    JPanel panel_remove_node = new  JPanel();
	    panel_remove_node.setLayout(new BoxLayout(panel_remove_node, BoxLayout.LINE_AXIS));
	    TitledBorder panel_remove_node_title = BorderFactory.createTitledBorder("Remove node");
	    panel_remove_node_title.setTitleJustification(TitledBorder.LEFT);
	    panel_remove_node.setBorder(panel_remove_node_title);
	    panel_remove_node.setPreferredSize(new Dimension(400, 48));
	    panel_remove_node.setMaximumSize(new Dimension(400, 48));
	    remove_node_cb.setPreferredSize(new Dimension(240, 25));
	    JButton remove_node_btn = new JButton("Remove");
	    remove_node_btn.addActionListener(new removeNode());
	    remove_node_btn.setPreferredSize(new Dimension(90, 25));
	    // Ajout des composants au sous-panel
	    panel_remove_node.add(remove_node_cb);
	    panel_remove_node.add(remove_node_btn);

	    // Sous-panel d'ajout d'une arete
	    JPanel panel_add_edge = new JPanel();
	    panel_add_edge.setLayout(new BoxLayout(panel_add_edge, BoxLayout.LINE_AXIS));
	    TitledBorder panel_add_edge_title = BorderFactory.createTitledBorder("Add edges");
	    panel_add_edge_title.setTitleJustification(TitledBorder.LEFT);
		panel_add_edge.setBorder(panel_add_edge_title);
		panel_add_edge.setPreferredSize(new Dimension(400, 48));
		panel_add_edge.setMaximumSize(new Dimension(400, 48));
	    start_node_cb.setPreferredSize(new Dimension(80, 25));
	    end_node_cb.setPreferredSize(new Dimension(80, 25));
	    edge_weight_sp.setPreferredSize(new Dimension(80, 25));
	    edge_weight_sp.setToolTipText("Poids");
	    SpinnerNumberModel snm = new SpinnerNumberModel(1, 0, 8000, 1);
	    edge_weight_sp.setModel(snm);
	    JButton add_edge_btn = new JButton("Add");
	    add_edge_btn.addActionListener(new addEdge());
	    add_edge_btn.setPreferredSize(new Dimension(90, 25));
	    // Ajout des composants au sous-panel
	    panel_add_edge.add(start_node_cb);
	    panel_add_edge.add(end_node_cb);
	    panel_add_edge.add(edge_weight_sp);
	    panel_add_edge.add(add_edge_btn);

	    // Sous-panel de suppression d'une arete
	    JPanel panel_remove_edge = new  JPanel();
	    panel_remove_edge.setLayout(new BoxLayout(panel_remove_edge, BoxLayout.LINE_AXIS));
	    TitledBorder panel_remove_edge_title = BorderFactory.createTitledBorder("Remove edge");
	    panel_remove_edge_title.setTitleJustification(TitledBorder.LEFT);
	    panel_remove_edge.setBorder(panel_remove_edge_title);
	    panel_remove_edge.setPreferredSize(new Dimension(400, 48));
	    panel_remove_edge.setMaximumSize(new Dimension(400, 48));
	    remove_edge_cb.setPreferredSize(new Dimension(240, 25));
	    JButton remove_edge_btn = new JButton("Remove");
	    remove_edge_btn.addActionListener(new removeEdge());
	    remove_edge_btn.setPreferredSize(new Dimension(90, 25));
	    // Ajout des composants au sous-panel
	    panel_remove_edge.add(remove_edge_cb);
	    panel_remove_edge.add(remove_edge_btn);

	    // Sous-panel de'import d'un fichier dgs
	    JPanel panel_load_file = new  JPanel();
	    panel_load_file.setLayout(new BoxLayout(panel_load_file, BoxLayout.LINE_AXIS));
	    TitledBorder panel_load_file_title = BorderFactory.createTitledBorder("Load File");
	    panel_load_file_title.setTitleJustification(TitledBorder.LEFT);
	    panel_load_file.setBorder(panel_load_file_title);
	    panel_load_file.setPreferredSize(new Dimension(400, 48));
	    panel_load_file.setMaximumSize(new Dimension(400, 48));
	    JButton select_file = new JButton("Browse");
	    select_file.addActionListener(new selectFile());
	    load_file_tf.setPreferredSize(new Dimension(400, 25));
	    load_file_tf.setEditable(false);
	    load_file_tf.addActionListener(new selectFile());
	    // Ajout des composants au sous-panel
	    panel_load_file.add(load_file_tf);
	    panel_load_file.add(Box.createHorizontalGlue());
	    panel_load_file.add(select_file);

	    // Ajout des sous-panel au panel
	    panel.add(panel_add_node);
	    panel.add(panel_remove_node);
	    panel.add(panel_add_edge);
	    panel.add(panel_remove_edge);
	    panel.add(Box.createVerticalGlue());
	    panel.add(panel_load_file);

	    // Ajout de notre panel à l'ouest de notre fenetre
	    this.add(panel, BorderLayout.WEST);
	}

	/**
	 * Ecouteur qui ouvre une fenetre de selection d'un fichier de type "dgs"
	 * lorsqu'il est solicité
	 * 
	 * @author Corentin Ballot
	 */
	private class selectFile implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			try{
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Dump Graph Stream files", "dgs");
				chooser.setFileFilter(filter);
				int returnVal = chooser.showOpenDialog(null);
				if(returnVal == JFileChooser.APPROVE_OPTION) {		
					load_file_tf.setText(chooser.getSelectedFile().getPath());
					g.read(chooser.getSelectedFile().getPath());
					updateComboNodeList();
					updateComboEdgeList();
					
					Iterator<Edge> it = g.getEdgeIterator();
					while(it.hasNext()){
						Edge edge = it.next();
						edge.addAttribute("ui.label", edge.getAttribute("weight") + "");
					}

					Iterator<Node> it2 = g.getNodeIterator();
					while(it2.hasNext()){
						Node node = it2.next();
						node.addAttribute("ui.label", node.getId());
					}
				}
			} catch (ElementNotFoundException ex) {
				JOptionPane.showMessageDialog(null, ex.getMessage());
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(null, ex.getMessage());
			} catch (GraphParseException ex) {
				JOptionPane.showMessageDialog(null, ex.getMessage());
			}
		}
	}
	
	/**
	 * Met a jour les tables de routage de chaque noeud
	 */
	private void updateRootingTables(){
		APSP apsp = new APSP();
 		apsp.init(g); // registering apsp as a sink for the graph
 		apsp.setDirected(false); // undirected graph
 		apsp.setWeightAttributeName("weight"); // ensure that the attribute name used is "weight"
 
 		apsp.compute(); // the method that actually computes shortest paths
	}

	/**
	 * Ecouteur qui met à jour le JTable 
	 * lorsqu'il est solicité
	 * 
	 * @author Corentin Ballot
	 */
	private class updateRoutingTable implements ActionListener{
		public void actionPerformed(ActionEvent arg0) {
			try{
				updateRootingTables();	
				Node selected =  g.getNode((String) routing_table_node_cb.getSelectedItem());
				String[][] table = new String[g.getNodeCount()-1][selected.getDegree()+1];
				
				HashMap<Edge, Integer> backup = new HashMap<>();
				for(Edge e: selected.getEachEdge()){
					backup.put(e, e.getAttribute("weight"));
				}
				
				Iterator<Node> nodes = g.getNodeIterator();
				int i = 0;
				while(nodes.hasNext()) {
					Node n = nodes.next();
					if(!n.equals(selected)){
						table[i][0] = n.getId() + " :";
						for (int j = 1; j < selected.getDegree()+1; j++) {
							APSPInfo info = selected.getAttribute(APSPInfo.ATTRIBUTE_NAME);
							
							table[i][j] = info.getShortestPathTo(n.getId()).getNodePath().get(1).getId();
							selected.getEdgeBetween(info.getShortestPathTo(n.getId()).getNodePath().get(1).getId()).setAttribute("weight", 2147483647);
							updateRootingTables();
						}
						i++;
						for(Edge e: selected.getEachEdge()){
							e.setAttribute("weight", backup.get(e));
						}
					}
				}
				
				routing_table_jt.setModel(new RoutingTableModel(table));
			}catch(Exception e){
				JOptionPane.showMessageDialog(null, "Le graphe doit être connexe");
			}
			
		}
	}
	
	/**
	 * Model de notre JTable
	 * 
	 * @author Corentin Ballot
	 */
	private class RoutingTableModel extends AbstractTableModel {
		
		private static final long serialVersionUID = 3763969283008554056L;
		String[][] table;
		
		public RoutingTableModel() {
			table = new String[0][0];
		}
		
		public RoutingTableModel(String[][] table) {
			this.table = table;
		}

		@Override
		public int getColumnCount() {
			return table.length>0 ? table[0].length : 0;
		}

		@Override
		public int getRowCount() {
			return table.length;
		}

		@Override
		public Object getValueAt(int arg0, int arg1) {
			return table[arg0][arg1];
		}
	}
	
	/**
	 * Ecouteur qui ajoute un noeud à notre graph, et met à jour les listes déroulantes 
	 * lorsqu'il est solicité
	 * 
	 * @author Corentin Ballot
	 */
	private class addNode implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			try{
				g.addNode(add_node_tf.getText());
				g.getNode(add_node_tf.getText()).addAttribute("ui.label", g.getNode(add_node_tf.getText()).getId());

				for(JComboBox<String> jcb : nodeLists)
					jcb.addItem(add_node_tf.getText());

				add_node_tf.setText("");
				add_node_tf.requestFocus();
			}catch(IdAlreadyInUseException ex){
				JOptionPane.showMessageDialog(null, ex.getMessage());
			}
		}
	}

	/**
	 * Ecouteur qui retire un noeud à notre graph, et met à jour les listes déroulantes 
	 * lorsqu'il est solicité
	 * 
	 * @author Corentin Ballot
	 */
	private class removeNode implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			try{
				g.removeNode((String) remove_node_cb.getSelectedItem());
				updateComboNodeList();
				updateComboEdgeList();
			}catch(ElementNotFoundException ex){
				JOptionPane.showMessageDialog(null, ex.getMessage());
			}
		}
	}

	/**
	 * Ecouteur qui ajoute une arete à notre graph, et met à jour les listes déroulantes 
	 * lorsqu'il est solicité
	 * 
	 * @author Corentin Ballot
	 */
	private class addEdge implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			try{
				g.addEdge((String) start_node_cb.getSelectedItem() + (String) end_node_cb.getSelectedItem(), (String) start_node_cb.getSelectedItem(), (String) end_node_cb.getSelectedItem());
				g.getEdge((String) start_node_cb.getSelectedItem() + (String) end_node_cb.getSelectedItem()).addAttribute("weight", edge_weight_sp.getValue());
				g.getEdge((String) start_node_cb.getSelectedItem() + (String) end_node_cb.getSelectedItem()).addAttribute("ui.label", edge_weight_sp.getValue());
				remove_edge_cb.addItem((String) start_node_cb.getSelectedItem() + (String) end_node_cb.getSelectedItem());
			}catch(ElementNotFoundException ex){
				JOptionPane.showMessageDialog(null, ex.getMessage());
			}
		}
	}

	/**
	 * Ecouteur qui retire une arete à notre graph, et met à jour les listes déroulantes 
	 * lorsqu'il est solicité
	 * 
	 * @author Corentin Ballot
	 */
	private class removeEdge implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			try{
				g.removeEdge((String) remove_edge_cb.getSelectedItem());
				updateComboEdgeList();
			}catch(ElementNotFoundException ex){
				JOptionPane.showMessageDialog(null, ex.getMessage());
			}
		}
	}

	/**
	 * Ecouteur qui ajoute aux noeuds et aretes du plus court chemin entre les deux noeuds des 
	 * listes déroulantes correspondante la classe css "pcc" lorsqu'il est solicité
	 * 
	 * @author Corentin Ballot
	 */
	private class displayPlusCourtChemin implements ActionListener {
		public void actionPerformed(ActionEvent arg0) {
			resetNodeAndEdgeClass();

			ArrayList<String> pcc = plusCourtChemin((String) routing_from_node_cb.getSelectedItem(),(String) routing_to_node_cb.getSelectedItem());

			g.getNode(pcc.get(0)).addAttribute("ui.class", "pcc");
			for (int i = 1; i < pcc.size(); i++) {
				g.getNode(pcc.get(i-1)).getEdgeBetween(pcc.get(i)).addAttribute("ui.class", "pcc");
				g.getNode(pcc.get(i)).addAttribute("ui.class", "pcc");
			}
		}
	}

	
	/**
	 * Retire le contenu de l'attribut ui.class de tout les noeuds et toutes les aretes du graph
	 */
	private void resetNodeAndEdgeClass() {
		Iterator<Edge> it = g.getEdgeIterator();
		while(it.hasNext()){
			it.next().addAttribute("ui.class", "");
		}

		Iterator<Node> it2 = g.getNodeIterator();
		while(it2.hasNext()){
			it2.next().addAttribute("ui.class", "");
		}
	}

	/**
	 * Met à jour les listes déroulantes contenant les noeuds du graph
	 */
	private void updateComboNodeList(){
		for(JComboBox<String> jcb : nodeLists)
			jcb.removeAllItems();

		Iterator<Node> it = g.getNodeIterator();
		while(it.hasNext()){
			String id = it.next().getId();
			for(JComboBox<String> jcb : nodeLists)
				jcb.addItem(id);
		}
	}

	/**
	 * Met à jour la liste déroulante contenant les aretes du graph
	 */
	private void updateComboEdgeList(){
		// Remove combo edge
		remove_edge_cb.removeAllItems();
		Iterator<Edge> it = g.getEdgeIterator();
		while(it.hasNext()){
			remove_edge_cb.addItem(it.next().getId());
		}
	}

	/**
	 * Calcul le plus court chemin entre deux noeud en utilisant l'algorithme de Dijkstra
	 * 
	 * @param start Noeud de départ du chemin
	 * @param to Noeud d'arrivé du chemin
	 * @return Une ArrayList contenant les noeuds du plus court chemin pour se rendre de start à to
	 */
	private ArrayList<String> plusCourtChemin(String start, String to) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		Map<String, String> pred = new HashMap<String, String>();
		ArrayList<String> temporaire = new ArrayList<String>();
		Iterator<Node> init = g.getNodeIterator();
		while(init.hasNext()){
			Node n = init.next();
			map.put(n.getId(), 2147483647);
			temporaire.add(n.getId());
		}

		map.put(start, 0);
		pred.put(start, null);
		
		try {
			while(!temporaire.isEmpty()) {
				String i = getMin(map, temporaire);
				temporaire.remove(i);
	
				init = g.getNode(i).getNeighborNodeIterator();
				while (init.hasNext()) {
					Node n = init.next();
	
					if(temporaire.contains(n.getId()) && map.get(n.getId()) > (map.get(i) + (Integer) n.getEdgeBetween(i).getAttribute("weight"))){
						map.put(n.getId(), map.get(i) + (Integer) n.getEdgeBetween(i).getAttribute("weight"));
						pred.put(n.getId(), i);
					}
				}
			}
	
			temporaire.add(to);
			while(!temporaire.get(temporaire.size()-1).equals(start)){
				temporaire.add(pred.get(temporaire.get(temporaire.size()-1)));
			}
		}catch(NullPointerException e) {
			JOptionPane.showMessageDialog(null, "Le graphe doit être connexe");
			return new ArrayList<>();
		}
		return temporaire;
		
	}

	/**
	 * Récupère la clé du noeud le plus petit appartenant à la liste temporaire
	 *
	 * @param map Objet Map à parcourir
	 * @param temporaire ArrayList contenant les noeud temporaire
	 * @return La clé appartenant à la liste temporaire dont la valeur est la plus petite
	 */
	private String getMin(Map<String, Integer> map, ArrayList<String> temporaire) {
		Entry<String, Integer> min = new Entry<String, Integer>() {
			public Integer setValue(Integer value) {return null;}
			public Integer getValue() {return 2147483647;}
			public String getKey() {return null;}
		};

		for (Entry<String, Integer> entry : map.entrySet()) {
		    if (temporaire.contains(entry.getKey()) && min.getValue() > entry.getValue()) {
		        min = entry;
		    }
		}
		return min.getKey();
	}

	public static void main(String[] args) {
		new Fenetre(); // Lance le programme
	}
}


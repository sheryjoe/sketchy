package mentalRep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Tree {
	
	// arraylist index is parent id; start from 0 for start symbol
	private ArrayList<HashMap<Integer,Integer>> ruleCounts;
	private final Random gen = new Random();
	private static final int[] primes = {2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59};

	private final double alpha;
	
	private ArrayList<Node> nodes;
	private final Node root;
	
	public Tree(int [] seq, double alpha, ArrayList<HashMap<Integer,Integer>> ruleCounts) {
		this.alpha = alpha;
		this.ruleCounts = ruleCounts;
		
		nodes = new ArrayList<Node>();
		
		for (int id: seq) {
			Node n = new Node(id);
			nodes.add(n);
			int ruleId = getRuleId(n);
			
			HashMap<Integer,Integer> rule = ruleCounts.get(id);

			if (rule.get(ruleId) == null)
				rule.put(ruleId,1);
			else
				rule.put(ruleId,rule.get(ruleId)+1);

		}
		root = nodes.get(0);
		
		initializeTree();
	}
	
	public double getSeqProb(double decayTrav) {
		ArrayList <Node> frontier = new ArrayList<Node>();
		ArrayList <Double> frontierWeights = new ArrayList<Double>();
		frontier.addAll(root.getChildren());
		for (int i = 0; i < frontier.size(); i++) {
			frontierWeights.add(1.0);
		}
		
		return root.seqProb(nodes, 1, frontier,frontierWeights,1.0,decayTrav);
	}
	
	/*
	 * should clone for safety-defensive copying.
	 */
	public ArrayList<HashMap<Integer,Integer>> getRuleCounts() {
		return ruleCounts;
	}
	
	public double sampleNewConfig(double decayTrav) {
		// Generate child between 2->last (no point resampling root and 2nd elem)
		int childId = gen.nextInt(nodes.size()-2)+2;
		Node child = nodes.get(childId);
		Node originalParent = child.parent;
		
		int newParentId = gen.nextInt(childId);
		Node newParent = nodes.get(newParentId);
		
		boolean accepted = accept(originalParent,newParent,child,decayTrav);
		if (accepted) {
			unlinkNode(child);
			linkNodes(newParent,child);
		}
		return accepted ? 1.0 : 0.0;
	}
	
	private boolean accept(Node originalParent, Node newParent, Node child, double decayTrav) {
		if(originalParent==newParent)
			return true;
		
		int origParent_origRule = getRuleId(originalParent);
		int origParent_newRule = getRuleId(originalParent)/primes[child.id];
		
		int newParent_origRule = getRuleId(newParent);
		int newParent_newRule = getRuleId(newParent)*primes[child.id];
			
		HashMap<Integer,Integer> origParentRules = ruleCounts.get(originalParent.id);
		HashMap<Integer,Integer> newParentRules = ruleCounts.get(newParent.id);
	
		double count_origPar_origRule = alpha+origParentRules.get(origParent_origRule);
		double count_newPar_origRule = alpha+newParentRules.get(newParent_origRule);
		
		Integer temp = origParentRules.get(origParent_newRule);
		double count_origPar_newRule = temp == null ? alpha : alpha+temp.intValue();
		temp = newParentRules.get(newParent_newRule);
		double count_newPar_newRule = temp == null ? alpha : alpha+temp.intValue();
		
		double acceptFactorTreeLike = (count_origPar_newRule/(count_origPar_origRule-1))
				                      *(count_newPar_newRule/(count_newPar_origRule-1));

		double acceptFactorSeq = 1/getSeqProb(decayTrav);
		
		// ugly
		unlinkNode(child);
		linkNodes(newParent,child);
		acceptFactorSeq *= getSeqProb(decayTrav);
		unlinkNode(child);
		linkNodes(originalParent,child);
		// ugly
		
		double acceptprob = acceptFactorTreeLike*acceptFactorSeq;			
		return acceptprob > gen.nextDouble(); 
	}
	
	private void initializeTree() {
		for (int i = nodes.size()-1; i>0; i--) {
			int parentNode = gen.nextInt(i);
			//int parentNode = i-1;
			linkNodes(nodes.get(parentNode),nodes.get(i));
		}
		
		ArrayList <Node> frontier = new ArrayList<Node>();
		ArrayList <Double> frontierWeights = new ArrayList<Double>();
		frontier.addAll(root.getChildren());
		for (int i = 0; i < frontier.size(); i++)
			frontierWeights.add(1.0);
		
		/*
		if(root.seqProb(nodes, 1, frontier,frontierWeights,1.0) == 0) {
			System.out.println(root.toSubTree());
			System.out.println(nodes);
			throw new RuntimeException("invalid initialized tree!");
		}
		*/
	}
	
	private void linkNodes(Node parent, Node child) {
		decrementRule(parent);
		parent.addChild(child);
		incrementRule(parent);
	}
	
	private void unlinkNode(Node child) {
		Node parent = child.parent;
		decrementRule(parent);
		parent.removeChild(child);
		incrementRule(parent);
	}
	
	private void decrementRule(Node parent) {
		HashMap<Integer,Integer> parentRules = ruleCounts.get(parent.id);
		int ruleId = getRuleId(parent);
		Integer count = parentRules.get(ruleId);
		if (count == null) {
			throw new RuntimeException("Rules does not exist: " + ruleId);
		}
		int newVal = count.intValue()-1;
		if (newVal == 0) {
			parentRules.remove(ruleId);
		} else {
			parentRules.put(ruleId, newVal);
		}
		return;
	}
	
	private void incrementRule(Node parent) {
		HashMap<Integer,Integer> parentRules = ruleCounts.get(parent.id);
		int ruleId = getRuleId(parent);
		Integer count = parentRules.get(ruleId);

		int oldVal = 0;
		if (count != null) {
			oldVal = count.intValue();
		}
		parentRules.put(ruleId, oldVal+1);
		return;
	}
	
	/*
	 * 1 is the special 'stop' rule
	 */
	private int getRuleId(Node parent) {
		
		int res = 1;
		ArrayList<Node> children = parent.getChildren();
		for (Node c: children) {
			res *= primes[c.id];
		}
		return res;
	}
	
	public static ArrayList<Integer> getRuleList(int ruleId) {
		ArrayList<Integer> res = new ArrayList<Integer>();
		for (int i=0;i<primes.length;i++) {
			while (true) {
				if (ruleId % primes[i] != 0)
					break;;
				ruleId /= primes[i];
				res.add(i);		
			}
		}
		return res;
	}
	
	// stop rule not included (null terminator, in a sense)
	public static ArrayList<String> getRuleListStrings(int ruleId, ArrayList<String> partList) {
		ArrayList<String> res = new ArrayList<String>();
		ArrayList<Integer> rules = getRuleList(ruleId);
		
		for (int rule : rules) {
			res.add(partList.get(rule));
		}
		return res;
	}
	
	@Override
	public String toString() {
		return root.toSubTree();
	}
	
	
}

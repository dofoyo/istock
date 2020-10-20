package com.rhb.istock.selector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DimensionView {
	private String code;
	private String name;
	Map<String,Board> boards;

	public DimensionView(String code, String name) {
		super();
		this.code = code;
		this.name = name;
		boards = new HashMap<String,Board>();
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addBoard(String code, String name, Integer ratio, String status) {
		boards.put(code, new Board(code,name,ratio, status));
	}
	
	public void addItem(String boardCode, String itemCode, String itemName, String status) {
		Board board = boards.get(boardCode);
		if(board!=null) {
			board.addItem(itemCode, itemName, status);
		}
	}
	
	public List<Board> getBoards() {
		List<Board> bs = new ArrayList(boards.values());
		Collections.sort(bs, new Comparator<Board>() {

			@Override
			public int compare(Board o1, Board o2) {
				return o2.getRatio().compareTo(o1.getRatio());
			}
			
		});
		return bs;
	}

	class Board{
		private String code;
		private String name;
		private Integer ratio;
		private String status;  //1 - 持有, 0 - 未持有
		List<Item> items;
		
		public Board(String code, String name, Integer ratio, String status) {
			super();
			this.code = code;
			this.name = name;
			this.ratio = ratio;
			this.status = status;
			items = new ArrayList<Item>();
		}
		
		public void addItem(String code, String name, String status) {
			items.add(new Item(code,name, status));
		}
		
		public String getCode() {
			return code;
		}
		public String getName() {
			return name;
		}
		public Integer getRatio() {
			return ratio;
		}
		public List<Item> getItems() {
			return items;
		}

		public String getStatus() {
			return status;
		}
		
		public String getType() {
			if(status.equals("1")) {
				return "danger";
			}else {
				return "plain";
			}
		}
		
	}
	
	class Item{
		private String code;
		private String name;
		private String status;  //1 - 持有, 0 - 未持有
		
		public Item(String code, String name, String status) {
			super();
			this.code = code;
			this.name = name;
			this.status = status;
		}
		public String getCode() {
			return code;
		}
		public String getName() {
			return name;
		}
		public String getStatus() {
			return status;
		}
	}
}

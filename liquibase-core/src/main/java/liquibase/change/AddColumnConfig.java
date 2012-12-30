package liquibase.change;


public class AddColumnConfig extends ColumnConfig {
    private Position position;
    
	public Position getPosition() {
		return position;
	}

	public AddColumnConfig setPosition(Position position) {
		this.position = position;
		return this;
	}
	
	public static class Position {
	    private String afterColumn;
	    private String beforeColumn;
	    private Integer position;

	    public String getAfterColumn() {
			return afterColumn;
		}

		public void setAfterColumn(String afterColumn) {
			this.afterColumn = afterColumn;
		}
		
	    public String getBeforeColumn() {
			return beforeColumn;
		}

		public void setBeforeColumn(String beforeColumn) {
			this.beforeColumn = beforeColumn;
		}

		public Boolean getFirst() {
			return (position != null) ? position.equals(Integer.valueOf(1)) : null;
		}

		public void setFirst(Boolean first) {
			setPosition(Boolean.TRUE.equals(first) ? Integer.valueOf(1) : null);
		}

		public Integer getPosition() {
			return position;
		}

		public void setPosition(Integer position) {
			this.position = position;
		}
	}
}

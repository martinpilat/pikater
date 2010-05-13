package ontology.messages;

import jade.content.Concept;
import jade.util.leap.List;

public class Option implements Concept{

	private boolean _mutable;
	private Interval _range;
	private List _set;
	private boolean _is_a_set;
	private Interval _number_of_args;
	private String _data_type;
	private String _description;
	private String _name;
	private String _synopsis;
	
	// Methods required to use this class to represent the TASK role
	public void setMutable(boolean mutable) {
		_mutable=mutable;
	}
	public boolean getMutable() {
		return _mutable;
	}	
	public void setRange(Interval range) {
		_range=range;
	}
	public Interval getRange() {
		return _range;
	}
	public void setSet(List set) {
		_set=set;
	}
	public List getSet() {
		return _set;
	}
	public void setIs_a_set(boolean is_a_set) {
		_is_a_set=is_a_set;
	}
	public boolean getIs_a_set() {
		return _is_a_set;
	}	
	public void setNumber_of_args(Interval number_of_args) {
		_number_of_args=number_of_args;
	}
	public Interval getNumber_of_args() {
		return _number_of_args;
	}
	public void setData_type(String data_type) {
		_data_type=data_type;
	}
	public String getData_type() {
		return _data_type;
	}
	public void setDescription(String description) {
		_description=description;
	}
	public String getDescription() {
		return _description;
	}
	public void setName(String name) {
		_name=name;
	}
	public String getName() {
		return _name;
	}
	public void setSynopsis(String synopsis) {
		_synopsis=synopsis;
	}
	public String getSynopsis() {
		return _synopsis;
	}

}
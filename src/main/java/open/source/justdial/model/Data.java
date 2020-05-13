package open.source.justdial.model;

public class Data implements Comparable<Data> {
	
	private String name;
	private String phone;
	private String rating;
	private String votes;
	private String locality;
	private String city;
	
	public static final String firstLineForExcel = "\"Name\",\"Phone\",\"Rating\",\"Votes\",\"Locality\",\"City\"\n";
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		phone = phone.replace('(', ' ');
		phone = phone.replace(')', ' ');
		phone = phone.replace('-', ' ');
		this.phone = phone;
	}
	public String getRating() {
		return rating;
	}
	public void setRating(String rating) {
		this.rating = rating;
	}
	public String getVotes() {
		return votes;
	}
	public void setVotes(String votes) {
		this.votes = votes;
	}
	public String getLocality() {
		return locality;
	}
	public void setLocality(String locality) {
		this.locality = locality;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	
	public String toStringForExcel() {
		return "\"" + name + "\",\"" + phone + "\",\"" + rating + "\",\"" + votes + "\",\"" + locality + "\",\"" + city + "\"\n";
	}
	
	@Override
	public String toString() {
		return name + "," + phone + "," + rating + "," + votes + "," + locality + "," +  city;
	}
	
	@Override
	public int compareTo(Data data) {
		return toString().compareTo(data.toString());
	}
	
}

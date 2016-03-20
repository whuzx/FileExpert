package xcxin.filexpert;

public class FeFilePermission {
	
	public String type;
	public String user;
	public String group;
	public String permission_string;
	
	public boolean user_read;
	public boolean user_write;
	public boolean user_exec;

	public boolean group_read;
	public boolean group_write;
	public boolean group_exec;

	public boolean other_read;
	public boolean other_write;
	public boolean other_exec;
}
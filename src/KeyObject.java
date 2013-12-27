import java.io.Serializable;

public class KeyObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2037682953232690673L;
	private int num1, num2, num3, num4, num5;
	private String txt1, txt2, txt3, txt4, txt5;

	public KeyObject(int num1, int num2, int num3, int num4, int num5,
			String txt1, String txt2, String txt3, String txt4, String txt5) {
		// TODO Auto-generated constructor stub
		this.setNum1(num1);
		this.setNum2(num2);
		this.setNum3(num3);
		this.setNum4(num4);
		this.setNum5(num5);
		this.setTxt1(txt1);
		this.setTxt2(txt2);
		this.setTxt3(txt3);
		this.setTxt4(txt4);
		this.setTxt5(txt5);
	}

	public int getNum2() {
		return num2;
	}

	public void setNum2(int num2) {
		this.num2 = num2;
	}

	public String getTxt4() {
		return txt4;
	}

	public void setTxt4(String txt4) {
		this.txt4 = txt4;
	}

	public String getTxt5() {
		return txt5;
	}

	public void setTxt5(String txt5) {
		this.txt5 = txt5;
	}

	public String getTxt3() {
		return txt3;
	}

	public void setTxt3(String txt3) {
		this.txt3 = txt3;
	}

	public String getTxt2() {
		return txt2;
	}

	public void setTxt2(String txt2) {
		this.txt2 = txt2;
	}

	public String getTxt1() {
		return txt1;
	}

	public void setTxt1(String txt1) {
		this.txt1 = txt1;
	}

	public int getNum3() {
		return num3;
	}

	public void setNum3(int num3) {
		this.num3 = num3;
	}

	public int getNum5() {
		return num5;
	}

	public void setNum5(int num5) {
		this.num5 = num5;
	}

	public int getNum4() {
		return num4;
	}

	public void setNum4(int num4) {
		this.num4 = num4;
	}

	public int getNum1() {
		return num1;
	}

	public void setNum1(int num1) {
		this.num1 = num1;
	}

}

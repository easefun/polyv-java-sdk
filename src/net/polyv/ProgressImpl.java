package net.polyv;

public class ProgressImpl implements Progress {

	@Override
	public void run(long offset, long max) {
		// TODO Auto-generated method stub
		System.out.println(offset + "-" + max);

	}
}

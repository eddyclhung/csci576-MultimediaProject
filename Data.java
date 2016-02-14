public class Data {
	private int[] pos;
	private int mCluster = 0;

	public Data() {
	}

	public Data(int[] pos) {
		this.pos(pos);
	}

	public void pos(int[] pos) {
		this.pos = pos;
	}

	public int[] pos() {
		return this.pos;
	}

	public void cluster(int clusterNumber) {
		this.mCluster = clusterNumber;
	}

	public int cluster() {
		return this.mCluster;
	}
}

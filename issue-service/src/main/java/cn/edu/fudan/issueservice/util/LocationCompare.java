package cn.edu.fudan.issueservice.util;

import cn.edu.fudan.issueservice.domain.Location;
import cn.edu.fudan.issueservice.domain.RawIssue;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class LocationCompare {

    private static final double threshold_commonality = 0.95;
    private static final double threshold_overlapping = 0.9;
    private static final double threshold_lcs = 0.9;


    public static boolean isUniqueIssue(RawIssue issue_1, RawIssue issue_2) {
//		if(!issue_1.getType().equals(issue_2.getType()))
////			return false;
        if (!issue_1.equals(issue_2)) return false;
        List<Location> intersect_locations = intersect(issue_1.getLocations(), issue_2.getLocations());
        List<Location> intersect_locations2 = intersect(issue_2.getLocations(), issue_1.getLocations());
        List<Location> union_locations = union(issue_1.getLocations(), issue_2.getLocations());
        double commonality = (double) intersect_locations.size() / union_locations.size();

        if (commonality < threshold_commonality)
            return false;

        int count_overlapping = 0;
        for (Location location : intersect_locations) {
            //theSameItem( issue_1.contentOf(location), issue_2.contentOf((Location) it))
            for (Location location2 : intersect_locations2) { //may exist the situation of  n to n
                if (location2.equals(location) && theSameItem(location.getCode(), location2.getCode()))
                    count_overlapping++;
            }
        }

        return !((double)count_overlapping / union_locations.size() < threshold_overlapping);
	    /*
	    if (count_overlapping/union_locations.size()<threshold_overlapping)
	         return false;
	    return true;*/
    }

    private static List<Location> union(List<Location> locations1, List<Location> locations2) {
        // TODO Auto-generated method stub
        List<Location> unionList = new ArrayList<>();
        for (Location location : locations1) {
            unionList.add(location);
        }
        for (Location location : locations2) {
            if (!unionList.contains(location))
                unionList.add(location);
        }
        return unionList;
    }

    private static List<Location> intersect(List<Location> locations1, List<Location> locations2) {
        // TODO Auto-generated method stub
        List<Location> insertList = new ArrayList<>();
        //intersectSet's locations are from locations1
        for (Location location : locations1) {
            if (locations2.contains(location)) {
                insertList.add(location);
            }
        }

        return insertList;
    }

    private static boolean theSameItem(String content_1, String content_2) {
        return ((double) lcs(content_1, content_2)) / (double) Math.max(content_1.length(), content_2.length()) > threshold_lcs;
		/*
		   if (lcs(content_1, content_2)/Math.max(content_1.length(), content_2.length())>threshold_lcs)
		       return true;
		   return false;*/
    }

    /**
     * private static int lcs(String content_1,String content_2) {
     * <p>
     * char ch1[]=content_1.toCharArray();
     * char ch2[]=content_2.toCharArray();
     * <p>
     * int length1=ch1.length;
     * int length2=ch2.length;
     * <p>
     * int max=0;
     * int sign=0;
     * int[] mat=new int[length1];
     * <p>
     * for(int i=0;i<length2;i++){
     * for(int j=length1-1;j>=0;j--){
     * if(ch2[i]==ch1[j]){
     * if(i==0 || j==0){
     * mat[j]=1;
     * if(max==0){
     * max=1;
     * sign=j;
     * }
     * }
     * else{
     * mat[j]=mat[j-1]+1;
     * if(mat[j]>max){
     * max=mat[j];
     * sign=j;
     * }
     * }
     * }
     * else{
     * mat[j]=0;
     * }
     * }
     * }
     * return new String(ch1, sign-max+1, max).length();
     * }
     */

    private static int lcs(String x, String y) {

        char[] s1 = x.toCharArray();
        char[] s2 = y.toCharArray();
        int[][] array = new int[x.length() + 1][y.length() + 1];//此处的棋盘长度要比字符串长度多加1，需要多存储一行0和一列0

        for (int j = 0; j < array[0].length; j++) {//第0行第j列全部赋值为0
            array[0][j] = 0;
        }
        for (int i = 0; i < array.length; i++) {//第i行，第0列全部为0
            array[i][0] = 0;
        }

        for (int m = 1; m < array.length; m++) {//利用动态规划将数组赋满值
            for (int n = 1; n < array[m].length; n++) {
                if (s1[m - 1] == s2[n - 1]) {
                    array[m][n] = array[m - 1][n - 1] + 1;//动态规划公式一
                } else {
                    array[m][n] = max(array[m - 1][n], array[m][n - 1]);//动态规划公式二
                }
            }
        }
        Stack<Character> stack = new Stack<>();
        int i = x.length() - 1;
        int j = y.length() - 1;

        while ((i >= 0) && (j >= 0)) {
            if (s1[i] == s2[j]) {//字符串从后开始遍历，如若相等，则存入栈中
                stack.push(s1[i]);
                i--;
                j--;
            } else {
                if (array[i + 1][j] > array[i][j + 1]) {//如果字符串的字符不同，则在数组中找相同的字符，注意：数组的行列要比字符串中字符的个数大1，因此i和j要各加1
                    j--;
                } else {
                    i--;
                }
            }
        }
	/*
		while(!stack.isEmpty()){//打印输出栈正好是正向输出最大的公共子序列
			System.out.print(stack.pop());
		}*/
        return stack.size();
    }

    private static int max(int a, int b) {//比较(a,b)，输出大的值
        return (a > b) ? a : b;
    }
}

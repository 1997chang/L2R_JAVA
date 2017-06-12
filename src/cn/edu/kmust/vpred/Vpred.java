/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.edu.kmust.vpred;

import cn.edu.kmust.structplus.StructPlus;

/**
 *
 * @author Administrator
 */
public class Vpred {
    static final int V=16;
    private int feature_id;
    private float threshold;
    int []children=new int[2];
    
    public static int createNodes(StructPlus root,int i,Vpred []nodes){
        nodes[i]=new Vpred();
        nodes[i].feature_id=root.getFeature_id();
        nodes[i].threshold=root.getThreshold();
        if(root.getLeft()==null&&root.getRight()==null){
            nodes[i].children[0]=i;
            nodes[i].children[1]=i;
        }else{
            nodes[i].children[0]=i+1;
            int last=createNodes(root.getLeft(), i+1, nodes);
            nodes[i].children[1]=last+1;
            i=createNodes(root.getRight(), last+1, nodes);
        }
        return i;
    }
    
    public static Vpred[] changeTwoToOne(Vpred[][] nodes,long totalNodeSize,int []node_size_tree){
        Vpred []all_nodes=new Vpred[(int)totalNodeSize];
        int all_index=0;
        int treeSize=nodes.length;
        for(int i=0;i<treeSize;i++){
            int nsize=node_size_tree[i];
            node_size_tree[i]=all_index;
            int telement;
            for(telement=0;telement<nsize;telement++){
                all_nodes[all_index]=new Vpred();
                all_nodes[all_index].feature_id=nodes[i][telement].feature_id;
                all_nodes[all_index].threshold=nodes[i][telement].getThreshold();
                all_nodes[all_index].children[0]=nodes[i][telement].children[0];
                all_nodes[all_index].children[1]=nodes[i][telement].children[1];
                all_index++;
            }
        }
        return all_nodes;
    }

    /**
     * @return the threshold
     */
    public float getThreshold() {
        return threshold;
    }
    
    public void printtree(){
        System.out.println("当前节点feature为:"+feature_id);
        System.out.println("当前节点的阈值为:"+getThreshold());
        System.out.println("当前节点的左孩子下标是："+children[0]);
        System.out.println("当前节点的右孩子下标是："+children[1]);
    }
    
    public static void findLeaf(int deep,int []leaf,int numberOfFeatures,int numberOfinstances,final Vpred []nodes,final float []features,int shift){
        leaf[0]=nodes[shift].children[features[numberOfinstances*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[1]=nodes[shift].children[features[(numberOfinstances+1)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[2]=nodes[shift].children[features[(numberOfinstances+2)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[3]=nodes[shift].children[features[(numberOfinstances+3)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[4]=nodes[shift].children[features[(numberOfinstances+4)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[5]=nodes[shift].children[features[(numberOfinstances+5)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[6]=nodes[shift].children[features[(numberOfinstances+6)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[7]=nodes[shift].children[features[(numberOfinstances+7)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[8]=nodes[shift].children[features[(numberOfinstances+8)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[9]=nodes[shift].children[features[(numberOfinstances+9)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[10]=nodes[shift].children[features[(numberOfinstances+10)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[11]=nodes[shift].children[features[(numberOfinstances+11)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[12]=nodes[shift].children[features[(numberOfinstances+12)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[13]=nodes[shift].children[features[(numberOfinstances+13)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[14]=nodes[shift].children[features[(numberOfinstances+14)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        leaf[15]=nodes[shift].children[features[(numberOfinstances+15)*numberOfFeatures+nodes[shift].feature_id]>nodes[shift].threshold ? 1 : 0];
        for(int i=2;i<=deep;i++){
            leaf[0]=nodes[shift+leaf[0]].children[features[numberOfinstances*numberOfFeatures+nodes[shift+leaf[0]].feature_id]>nodes[shift+leaf[0]].threshold ? 1 : 0];
            leaf[1]=nodes[shift+leaf[1]].children[features[(numberOfinstances+1)*numberOfFeatures+nodes[shift+leaf[1]].feature_id]>nodes[shift+leaf[1]].threshold ? 1 : 0];
            leaf[2]=nodes[shift+leaf[2]].children[features[(numberOfinstances+2)*numberOfFeatures+nodes[shift+leaf[2]].feature_id]>nodes[shift+leaf[2]].threshold ? 1 : 0];
            leaf[3]=nodes[shift+leaf[3]].children[features[(numberOfinstances+3)*numberOfFeatures+nodes[shift+leaf[3]].feature_id]>nodes[shift+leaf[3]].threshold ? 1 : 0];
            leaf[4]=nodes[shift+leaf[4]].children[features[(numberOfinstances+4)*numberOfFeatures+nodes[shift+leaf[4]].feature_id]>nodes[shift+leaf[4]].threshold ? 1 : 0];
            leaf[5]=nodes[shift+leaf[5]].children[features[(numberOfinstances+5)*numberOfFeatures+nodes[shift+leaf[5]].feature_id]>nodes[shift+leaf[5]].threshold ? 1 : 0];
            leaf[6]=nodes[shift+leaf[6]].children[features[(numberOfinstances+6)*numberOfFeatures+nodes[shift+leaf[6]].feature_id]>nodes[shift+leaf[6]].threshold ? 1 : 0];
            leaf[7]=nodes[shift+leaf[7]].children[features[(numberOfinstances+7)*numberOfFeatures+nodes[shift+leaf[7]].feature_id]>nodes[shift+leaf[7]].threshold ? 1 : 0];
            leaf[8]=nodes[shift+leaf[8]].children[features[(numberOfinstances+8)*numberOfFeatures+nodes[shift+leaf[8]].feature_id]>nodes[shift+leaf[8]].threshold ? 1 : 0];
            leaf[9]=nodes[shift+leaf[9]].children[features[(numberOfinstances+9)*numberOfFeatures+nodes[shift+leaf[9]].feature_id]>nodes[shift+leaf[9]].threshold ? 1 : 0];
            leaf[10]=nodes[shift+leaf[10]].children[features[(numberOfinstances+10)*numberOfFeatures+nodes[shift+leaf[10]].feature_id]>nodes[shift+leaf[10]].threshold ? 1 : 0];
            leaf[11]=nodes[shift+leaf[11]].children[features[(numberOfinstances+11)*numberOfFeatures+nodes[shift+leaf[11]].feature_id]>nodes[shift+leaf[11]].threshold ? 1 : 0];
            leaf[12]=nodes[shift+leaf[12]].children[features[(numberOfinstances+12)*numberOfFeatures+nodes[shift+leaf[12]].feature_id]>nodes[shift+leaf[12]].threshold ? 1 : 0];
            leaf[13]=nodes[shift+leaf[13]].children[features[(numberOfinstances+13)*numberOfFeatures+nodes[shift+leaf[13]].feature_id]>nodes[shift+leaf[13]].threshold ? 1 : 0];
            leaf[14]=nodes[shift+leaf[14]].children[features[(numberOfinstances+14)*numberOfFeatures+nodes[shift+leaf[14]].feature_id]>nodes[shift+leaf[14]].threshold ? 1 : 0];
            leaf[15]=nodes[shift+leaf[15]].children[features[(numberOfinstances+15)*numberOfFeatures+nodes[shift+leaf[15]].feature_id]>nodes[shift+leaf[15]].threshold ? 1 : 0];
        }
    }
    
}

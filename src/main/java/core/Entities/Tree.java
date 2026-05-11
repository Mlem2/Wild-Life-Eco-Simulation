package Entities;

import AllEnum.Direction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Tree extends Entity{
    private int age;
    private int seedCD=150;
    private int growthTime;
    public Tree(String name, int x, int y, int age){
        super(name,x,y);
        this.age=age;
    }
    public void checkCD(Entity toaDoSV[][], List<Entity> allEntities){
        if(growthTime==0){
            seedCD--;
            if(seedCD<=0){
                int treecount=0;
                int i2=Math.min(x+2,499);
                int j2=Math.min(y+2,499);
                for(int i1=Math.max(x-2,0);i1<=i2;i1++){
                    for(int j1=Math.max(y-2,0);j1<=j2;j1++){
                        if(toaDoSV[i1][j1] instanceof Tree && toaDoSV[i1][j1]!=null){
                            treecount++;
                        }
                    }
                }
                if(treecount<4){
                    Integer[] huong = {0,1,2,3,4,5,6,7,8};
                    Collections.shuffle(Arrays.asList(huong));
                    for(int direct:huong){
                        int tmp1=Direction.values()[direct].x;
                        int tmp2=Direction.values()[direct].y;
                        if(x+tmp1>=0 && x+tmp1<=499 && y+tmp2>=0 && y+tmp2<=499) {
                            if (toaDoSV[x + tmp1][y + tmp2] == null) {
                                Tree tmp = new Tree("", x + tmp1, y + tmp2, 50);
                                toaDoSV[x + tmp1][y + tmp2] = tmp;
                                allEntities.add(tmp);
                                seedCD = 150;
                                break;
                            }
                        }
                    }
                }
            }
        }
        else growthTime--;
    }

}
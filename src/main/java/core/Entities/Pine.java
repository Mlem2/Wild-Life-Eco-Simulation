package Entities;

import AllEnum.Direction;
import Entities.Base.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Pine extends Tree {
    public Pine(String name, int x, int y){
        super(name,x,y);
        this.seedCD1 = (ran.nextInt(6)+2)*21600;
        this.seedCD2 = seedCD1;
        this.growthTime = (ran.nextInt(3)+2)*21600;
        this.age = (ran.nextInt(3)+6)*21600;
    }

    public void checkCD(Entity[][] toaDoSV, List<Entity> allEntities){
        if(age<=0){
            this.isAlive = false;
            return;
        }
        else{
            if(growthTime<=0){
                seedCD2--;
                if(seedCD2<=0){
                    int treecount=0;
                    int i2 = Math.min(x+3,499);
                    int j2 = Math.min(y+3,499);
                    for(int i1 = Math.max(x-3,0);i1 <= i2;i1++){
                        for(int j1 = Math.max(y-3,0);j1 <= j2;j1++){
                            if(toaDoSV[i1][j1] instanceof Tree && toaDoSV[i1][j1]!=null){
                                treecount++;
                            }
                        }
                    }
                    if(treecount<4){
                        Integer[] huong = {0,1,2,3,4,5,6,7};
                        Collections.shuffle(Arrays.asList(huong));
                        for(int direct:huong){
                            int tmp1= Direction.values()[direct].x;
                            int tmp2=Direction.values()[direct].y;
                            if(x+tmp1>=0 && x+tmp1<=499 && y+tmp2>=0 && y+tmp2<=499) {
                                if (toaDoSV[x + tmp1][y + tmp2] == null) {
                                    Pine tmp = EntityFactory.CreateEntity(Pine::new,name,x+tmp1,y+tmp2);
                                    toaDoSV[x + tmp1][y + tmp2] = tmp;
                                    allEntities.add(tmp);
                                    seedCD2 = seedCD1;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            else growthTime--;
            age--;
        }
    }
}

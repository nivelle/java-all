package com.nivelle.core.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Menu extends Compont {

    private Long id;

    private String menuName;

    public Menu(Long id, String menuName) {
        this.id = id;
        this.menuName = menuName;
    }


    @Override
    public void canShow() {
        System.out.println("菜单可以展示出来！" + this.menuName);
    }
}

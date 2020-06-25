package com.nivelle.rpc.dubbo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Menu extends Compont {

    private Long id;

    private String menuName;

    @Override
    public void canShow() {
        System.out.println("菜单可以展示出来！" + this.menuName);
    }
}

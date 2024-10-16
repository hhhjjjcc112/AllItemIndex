# 目的
专门为某位的mc服务器上的全物品分类开发的模组，目的是实现简单的增删改查功能
# pfind/pf
查询与输入内容最匹配的若干个单片位置信息 \
形式为
```
/pfind <query> (<limit>)
```
query: 查询词语, 可以是中文、英文、拼音全称、拼音缩写, 支持模糊查询 \
limit: 输出的结果数, 可选, 默认为5

# pload/pl
尝试重新加载本地的index.json文件 \
形式为
```
/pload
```
该指令只能由管理员调用

# padd/pa
由3条子指令组成, 分别如下

## 1./padd alias
为指定的中文词语添加别名 \
形式为
```
/padd alias <alias> for <chineseName>
```
alias: 别名 \
chineseName: 中文名 \
注意: 如果当前全物品中没有对应名称的物品, 会拒绝执行. 

## 2./padd item exist
为已经存在于全物品分类中的物品添加一个另一个位置的单片信息 \
形式为
```
/padd item exist <chineseName> [floorLight] [direction] [carpetColor]
```
chineseName: 中文名 \
floorLight: 单片所在楼层的灯光方块(青翠蛙明体, 赭黄蛙明体, 珠光蛙明体, 海晶灯, 菌光体) \
direction: 单片的水平方向(东, 南, 西, 北) \
carpetColor: 单片地面地毯颜色(这我就不罗列了吧...) \
注意: 如果当前全物品中没有对应名称的物品, 会拒绝执行. 

## 3./padd item new
为尚不存在于全物品分类中的物品添加一个单片信息
形式为
```
/padd item new <chineseName> <englishName> [floorLight] [direction] [carpetColor]
```
chineseName: 中文名 \
englishName: 英文名 \
floorLight: 单片所在楼层的灯光方块 \
direction: 单片的水平方向 \
carpetColor: 单片地面地毯颜色 \
注意: 如果当前全物品中有对应名称的物品, 会拒绝执行. 
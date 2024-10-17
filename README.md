# 目的
专门为某位的mc服务器上的全物品分类开发的模组，目的是实现简单的增删改查功能
# pfind/pf
查询与输入内容最匹配的若干个单片位置信息
```
/pfind <query> (<limit>)
```
query: 查询词语, 可以是中文、英文、拼音全称、拼音缩写, 支持模糊查询 \
limit: 输出的结果数, 可选, 默认为5

# pload/pl
尝试重新加载本地的index.json文件
```
/pload
```
该指令只能由管理员调用

# padd/pa
为全物品分类添加信息 \
由2条子指令组成, 均只能由管理员执行, 分别如下

## 1./padd alias
为指定的中文词语添加别名 
```
/padd alias <alias> for <chineseName>
```
alias: 别名 \
chineseName: 中文名

## 2./padd item
为已经存在于全物品分类中的物品添加一个另一个位置的单片信息 \
形式总共有2种 

第1种形式为已经存在于全物品分类中的物品添加一个单片信息 
```
/padd item exist <chineseName> [floorLight] [direction] [carpetColor]
```
chineseName: 中文名(不支持别名) \
floorLight: 单片所在楼层的灯光方块(青翠蛙明体, 赭黄蛙明体, 珠光蛙明体, 海晶灯, 菌光体) \
direction: 单片的水平方向(东, 南, 西, 北) \
carpetColor: 单片地面地毯颜色(这我就不罗列了吧...) 

第2种形式为尚不存在于全物品分类中的物品添加一个单片信息
```
/padd item new <chineseName> <englishName> [floorLight] [direction] [carpetColor]
```
chineseName: 中文名(不支持别名) \
englishName: 英文名 \
floorLight: 单片所在楼层的灯光方块 \
direction: 单片的水平方向 \
carpetColor: 单片地面地毯颜色 

# premove/pr
为全物品分类删除信息 \
由2条子指令组成, 均只能由管理员执行, 分别如下

## 1./premove alias
删除指定的中文词语的别名
```
/premove alias <alias> from <chineseName>
```
alias: 别名 \
chineseName: 中文名 \

## 2./premove item
删除指定的中文词语的单片信息 \
有4种形式

第1种形式用于删除指定的中文词语对应的唯一一个单片
```
/premove item name <chineseName>
```
chineseName: 中文名(不支持别名) \
注意: 该指令会首先确认该物品只对应一片单片然后删除. 如果该物品有多片单片, 会仅输出所有对应单片的信息, 此时请使用下面的其他方式删除

第2种形式用于删除指定的中文词语对应的所有单片信息
```
/premove item name <chineseName> all
```
chineseName: 中文名(不支持别名) \

第3种形式用于删除指定的中文词语对应的指定单片信息
```
/premove item name <chineseName> <index>
```
chineseName: 中文名(不支持别名) \
index: 单片的索引(从1开始) \
注意: 该形式应该配合第1种形式或者查询使用, 以获取索引

第4种形式用于删除指定的id对应的指定单片信息
```
/premove item id <id>
```
id: 单片的id 
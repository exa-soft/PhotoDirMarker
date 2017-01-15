/**
 * 
 */
package ch.ebexasoft.fototools


def ahnin = new Person('Urahnin', 1850)
ahnin.presentMe()

a = new Person('Agatha', 1876)
ahnin.children.add(a)

b = new Person('Berta', 1905)
a.children.add(b)

d = new Person('Dorothee', 1907)
a.children.add(d)

ro = new Person('Rosemarie', 1936)
b.children.add(ro)

er = new Person('Erika', 1962)
yv = new Person('Yvonne', 1968)
an = new Person('Andrea', 1970)
ro.children.add(er)
ro.children.add(yv)
ro.children.add(an)

cl = new Person('Claire', 1944)
b.children.add(cl)

ed = new Person('Edith', 1969)
ri = new Person('Rita', 1972)
su = new Person('Susanne', 1974)
cl.children.add(ed)
cl.children.add(ri)
cl.children.add(su)

cl.presentMe()
cl.stellKinderVor()

ro.stellKinderVor()

cl.stellStammbaumVor()

ro.stellStammbaumVor()

ahnin.stellStammbaumVor()

ahnin.stellStammbaumVorVonUnten()

%Yongzuan Wu wu68 cs450 HW4 5.12
g=9.8065;
k=0.00341;
f = @(t)log(cosh(t*sqrt(g*k)))/k-1000;
t=fzero(f,10)

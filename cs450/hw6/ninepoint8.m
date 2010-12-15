%Yongzuan Wu wu68 cs450 HW6 9.8
format long;

D=3.844e8;
d=4.669e6;
earthx=[-d];
earthy=[0];
moonx=[D-d];
moony=[0];

%solve ode 
reltol=1e-1;
for i=1:6,
option=odeset('RelTol',reltol);
reltol=reltol/10;
[T,Y]=ode45(@threebody,[0 2400000], [4.613e8 0 0 -1074],option);
xt=Y(:,1)';
yt=Y(:,3)';
figure(i);
plot(xt,yt,'-',earthx,earthy,'o',moonx,moony,'o');
title(['relative error tolarence' num2str(reltol)]);
text(-d,0,'\leftarrow Earth',...
     'HorizontalAlignment','left');
text(D-d,0,'\leftarrow Moon',...
     'HorizontalAlignment','left');
end;

%output the steps
Step=T

disp('the stepsize becomes larger as the spacecraft approaches either moon or Earth, and becomes smaller when it gets further away');

%compute the minimun distance to earth
dis=sqrt((xt-(-d)).^2+(yt.^2))-6.378e6;
disp('the minimum distance from the spacecraft to earth is ');
mindistance=min(dis)




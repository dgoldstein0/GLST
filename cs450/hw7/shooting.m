function v=shooting(inislope)
reltol=1e-4;
option=odeset('RelTol',reltol);
sol=ode45(@ivp,[0 1],[0 inislope]',option);
[T,Y]=ode45(@ivp,[0 1],[0 inislope]',option);
x=deval(sol,1);
plot(T,Y);
v=x(1)-1;
end
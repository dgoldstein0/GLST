%y=[u  u']'
function dy=ivp(t,y)
dy=zeros(2,1);
dy(1)=y(2);
dy(2)=10*y(1).^3+3*y(1)+t.^2;

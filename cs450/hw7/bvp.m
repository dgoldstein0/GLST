%y=[u  u']'
function dy=bvp(t,y)
dy=zeros(4,1);
dy(1)=y(2);
dy(2)=10*y(1).^3+3*y(1)+t.^2;

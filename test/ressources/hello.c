int main()
{
 int x = 3;
 int y = 5;
 int z = (x + y);

 if (z > 3) {
    x = 7;
 }

 switch (z) {
    case 2: y = 1; break;
    case 5: y = 2; break;
    default: y = 3
 }
 return z*2;
 }

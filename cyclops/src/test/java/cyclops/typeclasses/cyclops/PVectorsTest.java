package cyclops.typeclasses.cyclops;

import static cyclops.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.oath.cyclops.hkt.Higher;
import cyclops.collections.immutable.VectorX;
import cyclops.control.Maybe;
import cyclops.function.Function1;
import cyclops.function.Lambda;
import com.oath.cyclops.hkt.DataWitness.vectorX;
import cyclops.typeclasses.functions.MonoidKs;
import org.junit.Test;



public class PVectorsTest {

    @Test
    public void unit(){

        VectorX<String> list = VectorX.VectorXInstances.unit()
                                     .unit("hello")
                                     .convert(VectorX::narrowK);

        assertThat(list,equalTo(VectorX.of("hello")));
    }
    @Test
    public void functor(){

        VectorX<Integer> list = VectorX.VectorXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> VectorX.VectorXInstances.functor().map((String v) ->v.length(), h))
                                     .convert(VectorX::narrowK);

        assertThat(list,equalTo(VectorX.of("hello".length())));
    }
    @Test
    public void apSimple(){
        VectorX.VectorXInstances.zippingApplicative()
            .ap(VectorX.of(l1(this::multiplyByTwo)), VectorX.of(1,2,3));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){

        VectorX<Function1<Integer,Integer>> listFn = VectorX.VectorXInstances.unit().unit(Lambda.l1((Integer i) ->i*2)).convert(VectorX::narrowK);

        VectorX<Integer> list = VectorX.VectorXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> VectorX.VectorXInstances.functor().map((String v) ->v.length(), h))
                                     .applyHKT(h-> VectorX.VectorXInstances.zippingApplicative().ap(listFn, h))
                                     .convert(VectorX::narrowK);

        assertThat(list,equalTo(VectorX.of("hello".length()*2)));
    }
    @Test
    public void monadSimple(){
       VectorX<Integer> list  = VectorX.VectorXInstances.monad()
                                      .flatMap(i-> VectorX.range(0,i), VectorX.of(1,2,3))
                                      .convert(VectorX::narrowK);
    }
    @Test
    public void monad(){

        VectorX<Integer> list = VectorX.VectorXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> VectorX.VectorXInstances.monad().flatMap((String v) -> VectorX.VectorXInstances.unit().unit(v.length()), h))
                                     .convert(VectorX::narrowK);

        assertThat(list,equalTo(VectorX.of("hello".length())));
    }
    @Test
    public void monadZeroFilter(){

        VectorX<String> list = VectorX.VectorXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> VectorX.VectorXInstances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(VectorX::narrowK);

        assertThat(list,equalTo(VectorX.of("hello")));
    }
    @Test
    public void monadZeroFilterOut(){

        VectorX<String> list = VectorX.VectorXInstances.unit()
                                     .unit("hello")
                                     .applyHKT(h-> VectorX.VectorXInstances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(VectorX::narrowK);

        assertThat(list,equalTo(VectorX.empty()));
    }

    @Test
    public void monadPlus(){
        VectorX<Integer> list = VectorX.VectorXInstances.<Integer>monadPlus()
                                      .plus(VectorX.empty(), VectorX.of(10))
                                      .convert(VectorX::narrowK);
        assertThat(list,equalTo(VectorX.of(10)));
    }
    @Test
    public void monadPlusNonEmpty(){

        VectorX<Integer> list = VectorX.VectorXInstances.<Integer>monadPlus(MonoidKs.vectorXConcat())
                                      .plus(VectorX.of(5), VectorX.of(10))
                                      .convert(VectorX::narrowK);
        assertThat(list,equalTo(VectorX.of(5,10)));
    }
    @Test
    public void  foldLeft(){
        int sum  = VectorX.VectorXInstances.foldable()
                        .foldLeft(0, (a,b)->a+b, VectorX.of(1,2,3,4));

        assertThat(sum,equalTo(10));
    }
    @Test
    public void  foldRight(){
        int sum  = VectorX.VectorXInstances.foldable()
                        .foldRight(0, (a,b)->a+b, VectorX.of(1,2,3,4));

        assertThat(sum,equalTo(10));
    }

    @Test
    public void traverse(){
       Maybe<Higher<vectorX, Integer>> res = VectorX.VectorXInstances.traverse()
                                                         .traverseA(Maybe.MaybeInstances.applicative(), (Integer a)->Maybe.just(a*2), VectorX.of(1,2,3))
                                                         .convert(Maybe::narrowK);


       assertThat(res,equalTo(Maybe.just(VectorX.of(2,4,6))));
    }

}

package cyclops.typeclasses;

import com.oath.cyclops.hkt.Higher;
import cyclops.arrow.Kleisli;
import cyclops.arrow.MonoidK;
import cyclops.control.Eval;
import cyclops.control.Maybe;
import cyclops.control.Option;
import cyclops.control.State;
import cyclops.data.LazySeq;
import cyclops.data.Seq;
import cyclops.data.tuple.Tuple2;
import cyclops.function.Function1;
import cyclops.function.Function2;
import cyclops.function.Function3;
import cyclops.function.Function4;
import cyclops.function.Function5;
import cyclops.function.Monoid;
import cyclops.function.Predicate3;
import cyclops.function.Predicate4;
import cyclops.function.Predicate5;
import cyclops.instances.control.OptionInstances;
import cyclops.instances.control.StateInstances;
import cyclops.reactive.ReactiveSeq;
import cyclops.typeclasses.foldable.Foldable;
import cyclops.typeclasses.foldable.Unfoldable;
import cyclops.typeclasses.functor.Compose;
import cyclops.typeclasses.functor.ContravariantFunctor;
import cyclops.typeclasses.functor.Functor;
import cyclops.typeclasses.monad.Applicative;
import cyclops.typeclasses.monad.Monad;
import cyclops.typeclasses.monad.MonadPlus;
import cyclops.typeclasses.monad.MonadZero;
import cyclops.typeclasses.monad.Traverse;
import lombok.AllArgsConstructor;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static cyclops.control.Option.some;
import static cyclops.data.tuple.Tuple.tuple;


public class Do<W> {

    private final Monad<W> monad;

    private Do(Monad<W> monad) {

        this.monad = monad;
    }

    public <T1> Do1<T1> __(Higher<W, T1> a) {
        return new Do1<>(a);
    }

    public <T1> Do1<T1> _of(T1 a) {
        return new Do1<>(monad.unit(a));
    }

    public <T1> Do1<T1> __(Supplier<Higher<W, T1>> a) {
        return new Do1<>(a.get());
    }

    public <T1> Do1<T1> flatten(Higher<W, Higher<W, T1>> nested){
        return new Do1<>(monad.flatten(nested));
    }
    public <T1,R> Kleisli<W,T1,R> kliesli( Function<? super T1, ? extends R> fn){
        return Kleisli.arrow(monad,fn);
    }
    public <T1,R> Kleisli<W,T1,R> kliesliK( Function<? super T1, ? extends Higher<W,R>> fn){
        return Kleisli.of(monad,fn);
    }

    public DoUnfolds expand(Unfoldable<W> unfolds){
        return new DoUnfolds(unfolds);
    }
    public DoUnfolds expand(Supplier<Unfoldable<W>> unfolds){
        return new DoUnfolds(unfolds.get());
    }
    @AllArgsConstructor
    public  class DoUnfolds{
        private final Unfoldable<W> unfolds;

        public <R, T> Do1<R> unfold(T b, Function<? super T, Option<Tuple2<R, T>>> fn){
            return __(unfolds.unfold(b, fn));
        }

        public <T> Do1<T> replicate(long n, T value) {
            return __(unfolds.replicate(n,value));
        }

        public <R> Do1<R> none() {
            return __(unfolds.none());
        }
        public <T> Do1<T> one(T a) {
            return __(unfolds.one(a));
        }
    }
    public <W2,T1> DoNested<W2,T1> __(Functor<W2> f, Higher<W, Higher<W2, T1>> nested){
        return new DoNested<>(nested,f);
    }
    public <W2,T1> DoNested<W2,T1> __(Supplier<Functor<W2>> f, Higher<W, Higher<W2, T1>> nested){
        return new DoNested<>(nested,f.get());
    }
    @AllArgsConstructor
    public class DoNested<W2,T1>{
        private final Higher<W, Higher<W2, T1>> nested;
        private final Compose<W,W2> f;

        public DoNested(Higher<W, Higher<W2, T1>> nested, Functor<W2> f2){
            this.nested =nested;
            this.f= Compose.compose(monad,f2);
        }


        public  Do<W2>.Do1<T1> foldK(Foldable<W> folds,Monad<W2> m2,MonoidK<W2> monoid) {
             return Do.forEach(m2).__(()->folds.foldK(monoid, nested));
        }
        public  Do<W2>.Do1<T1> foldK(Supplier<Foldable<W>> folds,Supplier<Monad<W2>> m2,MonoidK<W2> monoid) {
            return foldK(folds.get(),m2.get(),monoid);
        }
        public  Do<W>.Do1<T1> foldLeft(Foldable<W2> folds,Monoid<T1> monoid) {
            return __(()->monad.map_(nested,i->folds.foldLeft(monoid, i)));
        }
        public  Do<W>.Do1<T1> foldLeft(Supplier<Foldable<W2>> folds,Monoid<T1> monoid) {
            return foldLeft(folds.get(),monoid);
        }

        public <R> Do<W>.Do1<R> map(Function<? super Higher<W2,T1>, ? extends R> fn) {
            return __(()->monad.map_(nested,fn));
        }

        public Do<W2>.DoNested<W,T1> sequence(Traverse<W> traverse,Monad<W2> monad){
            return Do.forEach(monad).__(f.outer(),traverse.sequenceA(monad,nested));
        }



    }




    @FunctionalInterface
    public static interface Yield<T,R>{
        R yield(T t);
    }


    @AllArgsConstructor
    public class Do1<T1> {
        private final Higher<W, T1> a;



        public DoUnfolds1 expand(Unfoldable<W> unfolds){
            return new DoUnfolds1(unfolds);
        }
        @AllArgsConstructor
        public  class DoUnfolds1{
            private final Unfoldable<W> unfolds;

            public <R> Do2<R> unfold(Function<? super T1, Option<Tuple2<R, T1>>> fn){
                return __(b->unfolds.unfold(b, fn));
            }

            public Do2<T1> replicate(long n) {
                return __(value->unfolds.replicate(n,value));
            }

            public <R> Do2<R> none() {
                return __(t->unfolds.none());
            }
            public Do2<T1> one() {
                return __(a->unfolds.one(a));
            }
        }
        public <T2> Do2<T2> __(Higher<W, T2> b) {
            return new Do2<>(Function1.constant(b));
        }

        public <T2> Do2<T2> __(Function<T1,Higher<W, T2>> b) {
            return new Do2<>(b);
        }


        public <T2> Do2<T2> __(T2 b) {

            return new Do2<T2>(Function1.constant(monad.unit(b)));
        }
        public <T2> Do2<T2> _flatten(Higher<W, Higher<W, T2>> nested){
            return new Do2<>(in->monad.flatten(nested));
        }
        public Do1<T1> plus(MonadPlus<W> mp,Higher<W,T1> b){
            return new Do1<>(mp.plus(a,b));
        }
        public Do1<T1> plus(Supplier<MonadPlus<W>> mp,Higher<W,T1> b){
            return plus(mp.get(),b);
        }

        public <R> Do1<R> map(Function<? super T1, ? extends R> mapper){
            return new Do1<R>(monad.map_(a,mapper));
        }
        public <R> Do1<R> ap(Higher<W,Function<T1,R>> applicative){
            return new Do1<R>(monad.ap(applicative,a));
        }
        public Do1<T1> peek(Consumer<? super T1> mapper){
            return new Do1<>(monad.peek(mapper,a));
        }
        public <T2,R> Do1<R> zip(Higher<W, T2> fb, BiFunction<? super T1,? super T2,? extends R> f){
            return new Do1<>(monad.zip(a,fb,f));
        }
        public <T2,R,R2> Eval<R2> lazyZip(Supplier<Higher<W,T2>> lazy, BiFunction<? super T1,? super T2,? extends R> fn,Function<? super Higher<W,? extends R>,? extends R2> fold) {
            return monad.lazyZip(a,Eval.later(lazy),fn).map(in->fold.apply(in));
        }
        public <R> Yield<Function<? super T1,? extends R>, ? extends Higher<W, R>> guard(MonadZero<W> monadZero,Predicate<? super T1> fn) {
            return in->  new Do1<>(monadZero.filter(fn, a)).yield(in);
        }
        public <R> Yield<Function<? super T1,? extends R>, ? extends Do1<R>> doGuard(MonadZero<W> monadZero,Predicate<? super T1> fn) {
            return in->  new Do1<>(monadZero.filter(fn, a)).doYield(in);
        }
        public <R> Higher<W, R> yield(Function<? super T1,  ? extends R> fn) {
            return monad.map_(a, fn);

        }
        public <R> Do1<R> doYield(Function<? super T1,  ? extends R> fn) {
            Higher<W, R> hk = monad.map_(a, fn);
            return new Do1<R>(hk);
        }




        public Higher<W,T1> unwrap(){
            return a;
        }
        public <R> R fold(Function<? super Higher<W,T1>,? extends R> fn){
            return fn.apply(a);
        }


        @AllArgsConstructor
        public class DoFoldable{
            private final  Foldable<W> folds;

            public <R> R foldMap(final Monoid<R> mb, final Function<? super T1,? extends R> fn){
                return folds.foldMap(mb,fn,a);
            }

            public <R> R foldr(final Function< T1, Function< R, R>> fn, R r){
                return folds.foldr(fn,r,a);
            }


            public T1 foldRight(Monoid<T1> monoid){
                return folds.foldRight(monoid,a);
            }


            public T1 foldRight(T1 identity, BinaryOperator<T1> semigroup){
                return folds.foldRight(identity,semigroup,a);
            }

            public T1 foldLeft(Monoid<T1> monoid){
                return folds.foldLeft(monoid,a);
            }

            public T1 foldLeft(T1 identity, BinaryOperator<T1> semigroup){
                return folds.foldLeft(identity,semigroup,a);
            }
            public  long size() {
                return folds.size(a);
            }
            public  Seq<T1> seq(){
                return folds.seq(a);
            }
            public  LazySeq<T1> lazySeq(){
                return folds.lazySeq(a);
            }
            public  ReactiveSeq<T1> stream(){
                return folds.stream(a);
            }

            public   T1 intercalate(Monoid<T1> monoid, T1 value ){
                return seq().intersperse(value).foldLeft(monoid);
            }

            public   Option<T1> getAt(int index){
                return seq().get(index);
            }

            public   boolean anyMatch(Predicate<? super T1> pred){
                return folds.anyMatch(pred,a);
            }
            public   boolean allMatch(Predicate<? super T1> pred){
                return folds.allMatch(pred,a);
            }
        }

        public DoFoldable folds(Foldable<W> folds){
            return new DoFoldable(folds);

        }
        public <R> Do2<R> __fold(Foldable<W> folds,Function<? super DoFoldable,  ? extends R> fn){
            return __(in->monad.unit(fn.apply(folds(folds))));

        }

        public <R> Do1<R> contramap(ContravariantFunctor<W> cf,Function<? super R, ? extends T1> fn){
            return new Do1<>(cf.contramap(fn,a));
        }
        @AllArgsConstructor
        public class DoTraverse{

            private final Traverse<W> traverse;
            private <W2,R> Higher<W2, Higher<W, R>> traverse(Applicative<W2> applicative, Function<? super T1, ? extends Higher<W2, R>> fn){
                return traverse.traverseA(applicative,fn,a);
            }
            public <S,R,R2> State<S,R2> traverseS(Function<? super T1, ? extends State<S,R>> fn,Function<Higher<W,R>,R2> foldFn){
                return  State.narrowK(traverse(StateInstances.applicative(), fn)).map(foldFn);

            }
            public <S,R> Tuple2<S, Do1<R>> runTraverseS(Function<? super T1, ? extends State<S,R>> fn, S val) {
                return traverse.runTraverseS(fn,a,val).map2(i -> Do.forEach(monad).__(i));
            }
            public Do1<T1> reverse(){
                return Do.forEach(monad).__(traverse.reverse(a));
            }
            public  <S,R>  Tuple2<S, Do1<R>> mapAccumL (BiFunction<? super S, ? super T1, ? extends Tuple2<S,R>> f,S z) {
                return traverse.mapAccumL(f, a, z)
                                 .map2(i -> Do.forEach(monad).__(i));
            }
            public <R> R foldMap(Monoid<R> mb, final Function<? super T1,? extends R> fn) {
                return  traverse.foldMap(mb,fn,a);
            }
            public <R> Do1<R> mapWithIndex(BiFunction<? super T1,Long,? extends R> f) {
                return Do.forEach(monad)
                         .__(traverse.mapWithIndex(f,a));
            }

            public <W2,T2,R> Do1<R> zipWith(Foldable<W2> foldable, BiFunction<? super T1,? super Maybe<T2>,? extends R> f, Higher<W2, T2> ds2) {
                return Do.forEach(monad)
                         .__(traverse.zipWith(foldable,f,a,ds2));

            }
            public <R> Do1<Tuple2<T1,Long>> zipWithIndex() {
                return Do.forEach(monad)
                         .__(traverse.zipWithIndex(a));
            }
        }
        public DoTraverse traverse(Traverse<W> traverse){
            return new DoTraverse(traverse);
        }
        public Do1<T1> reverse(Traverse<W> traverse){
            return Do.forEach(monad).__(traverse.reverse(a));
        }
        public Do1<T1> reverse(Supplier<Traverse<W>> traverse){
            return reverse(traverse.get());
        }
        public Do1<Tuple2<T1,Long>> zipWithIndex(Traverse<W> traverse){
            return Do.forEach(monad).__(traverse.zipWithIndex(a));
        }
        public Do1<Tuple2<T1,Long>> zipWithIndex(Supplier<Traverse<W>>traverse){
            return zipWithIndex(traverse.get());
        }

        public String show(Show<W> show){
            return show.show(a);
        }
        public String show(){
            return new Show<W>(){}.show(a);
        }

        @AllArgsConstructor
        public class Do2<T2> {
            private final Function<T1,Higher<W, T2>> b;
            public <R> Do2<R> map(Function<? super T2, ? extends R> mapper){
                return new Do2<>(in->monad.map_(b.apply(in),mapper));
            }
            public String show(){
                return new Do1<>(monad.flatMap_(a,in->b.apply(in))).show();
            }
            public String show(Show<W> show){
                return new Do1<>(monad.flatMap_(a,in->b.apply(in))).show(show);
            }

            public <T3> Do3<T3> __(Higher<W, T3> c) {
                return new Do3<>(Function2.constant(c));
            }
            public <T3> Do3<T3> __(Supplier<Higher<W, T3>> c) {
                return new Do3<>(Function2.lazyConstant(c));
            }
            public <T3> Do3<T3> __(BiFunction<T1,T2,Higher<W, T3>> c) {
                return new Do3<>(c);
            }
            public <T3> Do3<T3> _1(Function<T1,Higher<W, T3>> c) {
                return new Do3<>(Function2.left(c));
            }
            public <T3> Do3<T3> _2(Function<T2,Higher<W, T3>> c) {
                return new Do3<>(Function2.right(c));
            }

            public <T3> Do3<T3> __(T3 c) {
                return new Do3<>(Function2.constant(monad.unit(c)));
            }

            public <R> Yield<BiFunction<? super T1, ? super T2,? extends R>, ? extends Higher<W,R>> guard(MonadZero<W> monadZero, BiPredicate<? super T1,? super T2> fn) {
                return in->  new Do2<>(t1->monadZero.filter(p->fn.test(t1,p), b.apply(t1))).yield(in);
            }
            public <R> Higher<W, R> yield(BiFunction<? super T1, ? super T2, ? extends R> fn) {
                Higher<W, R> hk = monad.flatMap_(a, in -> {


                    return monad.map_(b.apply(in), in2 -> fn.apply(in, in2));
                });
                return hk;
            }

            public <R,R1> R1 yield(BiFunction<? super T1, ? super T2, ? extends R> fn,Function<? super Higher<W,R>,? extends R1> fold) {
                Higher<W, R> hk = monad.flatMap_(a, in -> {


                    return monad.map_(b.apply(in), in2 -> fn.apply(in, in2));
                });
                return fold.apply(hk);
            }

            @AllArgsConstructor
            public class Do3<T3> {
                private final BiFunction<T1,T2,Higher<W, T3>> c;

                public <T4> Do4<T4> __(Higher<W, T4> d) {
                    return new Do4<>(Function3.constant(d));
                }
                public <T4> Do4<T4> __(Supplier<Higher<W, T4>> d) {
                    return new Do4<>(Function3.lazyConstant(d));
                }
                public <T4> Do4<T4> __(Function3<T1,T2,T3,Higher<W, T4>> c) {
                    return new Do4<>(c);
                }
                public <T4> Do4<T4> _0(Supplier<Higher<W, T4>> fn) {
                    return new Do4<>((a,b,c)->fn.get());
                }
                public <T4> Do4<T4> _1(Function<T1,Higher<W, T4>> fn) {
                    return new Do4<>((a,b,c)->fn.apply(a));
                }
                public <T4> Do4<T4> _2(Function<T2,Higher<W, T4>> fn) {
                    return new Do4<>((a,b,c)->fn.apply(b));
                }
                public <T4> Do4<T4> _3(Function<T3,Higher<W, T4>> fn) {
                    return new Do4<>((a,b,c)->fn.apply(c));
                }
                public <T4> Do4<T4> _12(BiFunction<T1,T2,Higher<W, T4>> fn) {
                    return new Do4<>((a,b,c)->fn.apply(a,b));
                }
                public <T4> Do4<T4> _23(BiFunction<T2,T3,Higher<W, T4>> fn) {
                    return new Do4<>((a,b,c)->fn.apply(b,c));
                }

                public <T4> Do4<T4> __(T4 d) {
                    return new Do4<>(Function3.constant(monad.unit(d)));
                }

                public <R> Yield<Function3<? super T1, ? super T2,? super T3,? extends R>, ? extends Higher<W,R>> guard(MonadZero<W> monadZero, Predicate3<? super T1,? super T2, ? super T3> fn) {
                    return in->  new Do3<>((t1,t2)->monadZero.filter(p->fn.test(t1,t2,p), c.apply(t1,t2))).yield(in);
                }
                public <R> Higher<W, R> yield(Function3<? super T1, ? super T2, ? super T3, ? extends R> fn) {
                    Higher<W, R> hk = monad.flatMap_(a, in -> {


                        Higher<W, R> hk2 = monad.flatMap_(b.apply(in), in2 -> {
                            Higher<W, R> hk3 = monad.map_(c.apply(in,in2), in3 -> fn.apply(in, in2, in3));
                            return hk3;
                        });
                        return hk2;
                    });
                    return hk;
                }

                @AllArgsConstructor
                public class Do4<T4> {
                    private final Function3<T1,T2,T3,Higher<W, T4>> d;

                    public <T5> Do5<T5> __(Higher<W, T5> e) {
                        return new Do5<>(Function4.constant(e));
                    }

                    public<T5> Do5<T5> __(Supplier<Higher<W, T5>> e) {
                        return new Do5<>(Function4.lazyConstant(e));
                    }
                    public <T5> Do5<T5> __(Function4<T1,T2,T3,T4,Higher<W, T5>> e) {
                        return new Do5<>(e);
                    }
                    public <T5> Do5<T5> _1(Function<T1,Higher<W, T5>> e) {
                        return new Do5<>((a,b,c,d)->e.apply(a));
                    }
                    public <T5> Do5<T5> _2(Function<T2,Higher<W, T5>> e) {
                        return new Do5<>((a,b,c,d)->e.apply(b));
                    }
                    public <T5> Do5<T5> _3(Function<T3,Higher<W, T5>> e) {
                        return new Do5<>((a,b,c,d)->e.apply(c));
                    }
                    public <T5> Do5<T5> _4(Function<T4,Higher<W, T5>> e) {
                        return new Do5<>((a,b,c,d)->e.apply(d));
                    }
                    public <T5> Do5<T5> _123(Function3<T1,T2,T3,Higher<W, T5>> e) {
                        return new Do5<>((a,b,c,d)->e.apply(a,b,c));
                    }
                    public <T5> Do5<T5> _234(Function3<T2,T3,T4,Higher<W, T5>> e) {
                        return new Do5<>((a,b,c,d)->e.apply(b,c,d));
                    }
                    public <T5> Do5<T5> _134(Function3<T1,T3,T4,Higher<W, T5>> e) {
                        return new Do5<>((a,b,c,d)->e.apply(a,c,d));
                    }
                    public <T5> Do5<T5> _124(Function3<T1,T2,T4,Higher<W, T5>> e) {
                        return new Do5<>((a,b,c,d)->e.apply(a,b,d));
                    }
                    public <T5> Do5<T5> _12(BiFunction<T1,T2,Higher<W, T5>> e) {
                        return new Do5<>((a,b,c,d)->e.apply(a,b));
                    }
                    public <T5> Do5<T5> _13(BiFunction<T1,T3,Higher<W, T5>> e) {
                        return new Do5<>((a,b,c,d)->e.apply(a,c));
                    }
                    public <T5> Do5<T5> _23(BiFunction<T2,T3,Higher<W, T5>> e) {
                        return new Do5<>((a,b,c,d)->e.apply(b,c));
                    }
                    public <T5> Do5<T5> _34(BiFunction<T3,T4,Higher<W, T5>> e) {
                        return new Do5<>((a,b,c,d)->e.apply(c,d));
                    }
                    public <T5> Do5<T5> _14(BiFunction<T1,T4,Higher<W, T5>> e) {
                        return new Do5<>((a,b,c,d)->e.apply(a,d));
                    }
                    public <T5> Do5<T5> _24(BiFunction<T2,T4,Higher<W, T5>> e) {
                        return new Do5<>((a,b,c,d)->e.apply(b,d));
                    }

                    public <T5> Do5<T5> __(T5 e) {
                        return new Do5<>(Function4.constant(monad.unit(e)));
                    }

                    public <R> Yield<Function4<? super T1, ? super T2,? super T3,? super T4,? extends R>, ? extends Higher<W,R>> guard(MonadZero<W> monadZero, Predicate4<? super T1,? super T2, ? super T3, ? super T4> fn) {
                        return in->  new Do4<>((t1,t2,t3)->monadZero.filter(p->fn.test(t1,t2,t3,p), d.apply(t1,t2,t3))).yield(in);
                    }
                    public <R> Higher<W, R> yield_13(Function2<? super T1, ? super T3, ? extends R> fn) {
                        Higher<W, R> hk = monad.flatMap_(a, in -> {


                            Higher<W, R> hk2 = monad.flatMap_(b.apply(in), in2 -> {
                                Higher<W, R> hk3 = monad.flatMap_(c.apply(in,in2), in3 -> {
                                    Higher<W, R> hk4 = monad.map_(d.apply(in,in2,in3), in4 -> fn.apply(in, in3));
                                    return hk4;
                                });
                                return hk3;
                            });
                            return hk2;
                        });
                        return hk;
                    }
                    public <R> Higher<W, R> yield(Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
                        Higher<W, R> hk = monad.flatMap_(a, in -> {


                            Higher<W, R> hk2 = monad.flatMap_(b.apply(in), in2 -> {
                                Higher<W, R> hk3 = monad.flatMap_(c.apply(in,in2), in3 -> {
                                    Higher<W, R> hk4 = monad.map_(d.apply(in,in2,in3), in4 -> fn.apply(in, in2, in3, in4));
                                    return hk4;
                                });
                                return hk3;
                            });
                            return hk2;
                        });
                        return hk;
                    }
                    @AllArgsConstructor
                    public class Do5<T5> {
                        private final Function4<T1,T2,T3,T4,Higher<W, T5>> e;
                        public <R> Yield<Function5<? super T1, ? super T2,? super T3,? super T4,? super T5,? extends R>, ? extends Higher<W,R>> guard(MonadZero<W> monadZero,
                                                                                                                                                      Predicate5<? super T1,? super T2, ? super T3, ? super T4, ? super T5> fn) {
                            return in->  new Do5<>((t1,t2,t3,t4)->monadZero.filter(p->fn.test(t1,t2,t3,t4,p), e.apply(t1,t2,t3,t4))).yield(in);
                        }
                        public <R> Higher<W, R> yield(Function5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5,? extends R> fn) {
                            Higher<W, R> hk = monad.flatMap_(a, in -> {


                                Higher<W, R> hk2 = monad.flatMap_(b.apply(in), in2 -> {
                                    Higher<W, R> hk3 = monad.flatMap_(c.apply(in,in2), in3 -> {
                                        Higher<W, R> hk4 = monad.flatMap_(d.apply(in,in2,in3), in4 -> {
                                            Higher<W,R> hk5 = monad.map_(e.apply(in,in2,in3,in4),in5->fn.apply(in, in2, in3, in4,in5));
                                            return hk5;
                                        });
                                        return hk4;
                                    });
                                    return hk3;
                                });
                                return hk2;
                            });
                            return hk;
                        }
                    }
                }
            }
        }


    }

    public static <W,T1> Do<W> forEach(Monad<W> a){
        return new Do(a);
    }

    public static <W,T1> Do<W> forEach(Supplier<Monad<W>> a){
        return forEach(a.get());
    }

    public static Maybe<String> opt(Number i){
        return null;
    }
    public static Maybe<String> op1(){
        return null;
    }

    public static void test(){
          Do.forEach(OptionInstances::monad)
            .__(some(10))
            .__(Do::opt);

        Do.forEach(OptionInstances::monad)
            .__(Do::op1);

        Do.forEach(OptionInstances::monad)
            ._of("hello");



    }
}
